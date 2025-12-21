package com.high.product.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.high.product.application.dto.external.OrderDetailResponse;
import com.high.product.application.dto.external.OrderItemResponse;
import com.high.product.application.dto.kafka.failure.StockDeductionFailMessage;
import com.high.product.application.dto.kafka.request.StockDeductionCommandRequest;
import com.high.product.application.dto.kafka.success.StockDeductionSuccessMessage;
import com.high.product.application.exception.ProductException;
import com.high.product.application.port.DistributedLockPort;
import com.high.product.application.port.OrderQueryPort;
import com.high.product.application.port.RedisCacheEvictPort;
import com.high.product.application.port.StockPublisherPort;
import com.high.product.domain.model.Product_Stock;
import com.high.product.domain.repository.StockRepository;
import com.high.product.exception.ProductErrorCode;
import com.high.product.application.port.SagaDeduplicationPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockDeductionService {

	private final OrderQueryPort orderQueryPort;
	private final StockRepository stockRepository;
	private final StockPublisherPort stockPublisherPort;
	private final DistributedLockPort distributedLockPort;
	private final SagaDeduplicationPort sagaDeduplicationPort;
	private final RedisCacheEvictPort stockCacheEvictPort;

	public void handleStockDeduction(StockDeductionCommandRequest request) {

		// 외부 조회 (락 밖)
		OrderDetailResponse order =
			orderQueryPort.getOrderDetail(request.orderId())
				.getBody()
				.data();

		List<String> lockKeys = order.orderItems().stream()
			.map(item -> "stock:product:" + item.productId())
			.toList();

		// 멀티 락
		distributedLockPort.executeWithMultiLock(lockKeys, () -> {

			log.info("재고 차감 처리 시작 sagaId={}", request.sagaId());

			// 멱등성 체크 (이미 완료된 saga)
			if (sagaDeduplicationPort.exists("success:" + request.sagaId())) {
				log.info("이미 성공 처리된 sagaId={}, success 재전송", request.sagaId());
				stockPublisherPort.publishSuccess(
					new StockDeductionSuccessMessage(
						request.sagaId(),
						request.orderId(),
						request.userId()
					)
				);
				return;
			}

			if (sagaDeduplicationPort.exists("fail:" + request.sagaId())) {
				log.info("이미 실패 처리된 sagaId={}, fail 재전송", request.sagaId());
				stockPublisherPort.publishFail(
					new StockDeductionFailMessage(
						request.sagaId(),
						request.orderId(),
						"이미 실패 처리된 saga",
						request.userId()
					)
				);
				return;
			}

			// 최초 처리 여부 판단
			if (!sagaDeduplicationPort.tryProcess("processing:" + request.sagaId(), 600)) {
				log.info("처리 중인 sagaId={}, skip", request.sagaId());
				return;
			}

			try {
				// 재고 차감 트랜잭션
				for (OrderItemResponse item : order.orderItems()) {
					Product_Stock stock = stockRepository
						.findByProductId(item.productId())
						.orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

					stock.reduce(item.quantity());
				}

				// DB 트랜잭션 이후 캐시 무효화
				for (OrderItemResponse item : order.orderItems()) {
					stockCacheEvictPort.evictStockCacheAfterCommit(item.productId());
				}

				// 성공 이벤트 + 멱등성 기록
				sagaDeduplicationPort.tryProcess("success:" + request.sagaId(), 600);
				sagaDeduplicationPort.remove("processing:" + request.sagaId());

				stockPublisherPort.publishSuccess(
					new StockDeductionSuccessMessage(
						request.sagaId(),
						request.orderId(),
						request.userId()
					)
				);

				log.info("재고 차감 성공 sagaId={}", request.sagaId());

			} catch (Exception ex) {

				sagaDeduplicationPort.tryProcess("fail:" + request.sagaId(), 600);
				sagaDeduplicationPort.remove("processing:" + request.sagaId());

				stockPublisherPort.publishFail(
					new StockDeductionFailMessage(
						request.sagaId(),
						request.orderId(),
						ex.getMessage(),
						request.userId()
					)
				);

				log.error("재고 차감 실패 sagaId={}", request.sagaId(), ex);
				throw ex;
			} finally {
				// processing 키 제거 (재처리 가능하도록)
				sagaDeduplicationPort.remove("processing:" + request.sagaId());
			}
		});
	}
}
