package com.high.product.infrastructure.kafka.consumer;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.product.application.service.SagaProcessingService;
import com.high.product.application.service.StockService;
import com.high.product.domain.model.Product_Stock;
import com.high.product.exception.InsufficientStockException;
import com.high.product.infrastructure.kafka.messaging.failure.StockReduceFailureItem;
import com.high.product.infrastructure.kafka.messaging.failure.StockReduceFailureMessage;
import com.high.product.infrastructure.kafka.messaging.request.StockReduceRequest;
import com.high.product.infrastructure.kafka.messaging.success.StockReduceSuccessItem;
import com.high.product.infrastructure.kafka.messaging.success.StockReduceSuccessMessage;
import com.high.product.infrastructure.kafka.producer.KafkaEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

	private final ObjectMapper objectMapper;
	private final StockService stockService;
	private final SagaProcessingService sagaProcessingService;
	private final KafkaEventPublisher kafkaEventPublisher;

	private static final String TOPIC_SUCCESS = "stock-reduce-success";
	private static final String TOPIC_FAILURE = "stock-reduce-failure";

	@KafkaListener(topics = "stock-deduction-success", groupId = "product-stock-consumer")
	public void onMessage(String message) {
		log.info("[Listener] received: {}", message);
		StockReduceRequest req;
		try {
			req = objectMapper.readValue(message, StockReduceRequest.class);
		} catch (Exception e) {
			log.error("[Listener] parse error, ignoring message: {}", message, e);
			return;
		}

		// Idempotency: mark processing if not yet processed
		boolean first = false;
		try {
			first = sagaProcessingService.markIfNotProcessed(req.sagaId(), req.orderId(), "ORDER_CREATE");
		} catch (Exception e) {
			log.warn("[Listener] idempotency check failed (will proceed): {}", e.getMessage());
			// DB PK 충돌 등은 아래 재처리/예외 로직으로 다룸.
			first = true; // 보수적 처리: 시도해본다.
		}
		if (!first) {
			log.info("[Listener] saga already processed sagaId={}", req.sagaId());
			return;
		}

		// 핵심: 로컬 트랜잭션에서 재고 변동 처리(트랜잭션은 stockService.reduceStock 내부에서 걸림)
		try {
			reduceAllItemsTransactional(req);
		} catch (InsufficientStockException ise) {
			log.info("[Listener] insufficient stock, send failure event: {}", ise.getMessage());
			var failureMsg = toFailureMessage(req, "INSUFFICIENT_STOCK");
			// 직접 동기 전송 시도 (재시도 필요하면 kafkaEventPublisher.sendAsyncWithRetry도 써라)
			try {
				kafkaEventPublisher.sendAsyncWithRetry(TOPIC_FAILURE, failureMsg)
					.whenComplete((v, ex) -> {
						if (ex != null)
							log.error("[Listener] failed to send failure event for insufficient stock", ex);
					});
			} catch (Exception e) {
				log.error("[Listener] error while initiating failure send", e);
			}
			return;
		} catch (Exception dbEx) {
			log.error("[Listener] DB error during stock reduce, send failure", dbEx);
			var failureMsg = toFailureMessage(req, "DB_ERROR");
			kafkaEventPublisher.sendAsyncWithRetry(TOPIC_FAILURE, failureMsg)
				.whenComplete((v, ex) -> {
					if (ex != null)
						log.error("[Listener] failed to send failure event after db error", ex);
				});
			return;
		}

		// 모든 아이템 차감이 정상적으로 완료되어 DB가 커밋될 예정.
		// 트랜잭션 커밋 후에 전송 작업이 시작되도록 afterCompletion hook 에 등록한다.
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				// 커밋 후: 비동기 전송 시작 (내부적으로 재시도 수행)
				var successMsg = toSuccessMessage(req);
				CompletableFuture<Void> fut = kafkaEventPublisher.sendAsyncWithRetry(TOPIC_SUCCESS, successMsg);

				fut.whenComplete((v, ex) -> {
					if (ex == null) {
						log.info("[Listener] success event published for sagaId={}", req.sagaId());
					} else {
						// 전송 실패 시 보상(재고 복원) 실행
						log.error("[Listener] success publish failed after retries. compensation needed. sagaId={}",
							req.sagaId(), ex);

						try {
							// restore each item in separate transaction
							restoreAllItems(req);
							// 보상 성공 후 failure 이벤트 발행 시도(동기 방식)
							var failureMsg = toFailureMessage(req, "KAFKA_PUBLISH_FAILED_AFTER_COMMIT");
							try {
								kafkaEventPublisher.sendSyncNoRetry(TOPIC_FAILURE, failureMsg);
							} catch (Exception syncEx) {
								// failure event 전송도 실패하면 로그/알람 필요
								log.error("[Listener] failed to send failure event after compensation", syncEx);
							}
						} catch (Exception restoreEx) {
							log.error("[Listener] compensation restore failed. Manual intervention required sagaId={}",
								req.sagaId(), restoreEx);
							// 운영 알람 필요
						}
					}
				});
			}
		});
	}

	// 모든 아이템 재고 차감(단일 트랜잭션)
	@Transactional
	protected void reduceAllItemsTransactional(StockReduceRequest req) {
		for (var item : req.itemList()) {
			stockService.reduceStock(item.productId(), item.quantity());
		}
	}

	// 보상: REQUIRES_NEW 트랜잭션 내부에서 each restore
	protected void restoreAllItems(StockReduceRequest req) {
		for (var item : req.itemList()) {
			stockService.restoreStock(item.productId(), item.quantity()); // REQUIRES_NEW
		}
	}

	// helper to convert DTOs
	private StockReduceSuccessMessage toSuccessMessage(StockReduceRequest req) {
		var items = req.itemList().stream().map(i -> new StockReduceSuccessItem(i.productId(), i.quantity())).toList();
		return new StockReduceSuccessMessage(req.orderId(), req.sagaId(), req.couponId(), req.ordererId(), items);
	}

	private StockReduceFailureMessage toFailureMessage(StockReduceRequest req, String reason) {
		var items = req.itemList().stream().map(i -> new StockReduceFailureItem(i.productId(), i.quantity())).toList();
		return new StockReduceFailureMessage(req.orderId(), req.sagaId(), req.couponId(), req.ordererId(), reason,
			items);
	}
}
