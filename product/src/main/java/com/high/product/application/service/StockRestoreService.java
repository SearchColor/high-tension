package com.high.product.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.high.product.application.dto.external.OrderDetailResponse;
import com.high.product.application.dto.external.OrderItemResponse;
import com.high.product.application.dto.kafka.failure.StockRestoreFailMessage;
import com.high.product.application.dto.kafka.request.StockRestoreCommandRequest;
import com.high.product.application.dto.kafka.success.StockRestoreSuccessMessage;
import com.high.product.application.exception.ProductException;
import com.high.product.application.port.DistributedLockPort;
import com.high.product.application.port.OrderQueryPort;
import com.high.product.application.port.StockPublisherPort;
import com.high.product.domain.model.Product_Stock;
import com.high.product.domain.repository.StockRepository;
import com.high.product.exception.ProductErrorCode;
import com.high.product.infrastructure.redis.SagaIdDeduplication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockRestoreService {

	private final OrderQueryPort orderQueryPort;
	private final StockRepository stockRepository;
	private final StockPublisherPort stockPublisherPort;
	private final DistributedLockPort distributedLockPort;
	private final SagaIdDeduplication sagaIdDeduplication;

	public void handleStockRestore(StockRestoreCommandRequest request) {

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

			log.info("재고 복원 처리 시작 sagaId={}", request.sagaId());

			// 이미 성공 처리된 복원
			if (sagaIdDeduplication.exists("restore:success:" + request.sagaId())) {
				log.info("이미 복원 성공 sagaId={}, success 재전송", request.sagaId());
				stockPublisherPort.publishSuccess(
					new StockRestoreSuccessMessage(
						request.sagaId(),
						request.orderId(),
						request.userId()
					)
				);
				return;
			}

			// 이미 실패 처리된 복원
			if (sagaIdDeduplication.exists("restore:fail:" + request.sagaId())) {
				log.info("이미 복원 실패 sagaId={}, fail 재전송", request.sagaId());
				stockPublisherPort.publishFail(
					new StockRestoreFailMessage(
						request.sagaId(),
						request.orderId(),
						"이미 실패 처리된 복원 saga",
						request.userId()
					)
				);
				return;
			}

			// 최초 처리 여부
			if (!sagaIdDeduplication.tryProcess("restore:processing:" + request.sagaId(), 600)) {
				log.info("복원 처리 중 sagaId={}, skip", request.sagaId());
				return;
			}

			try {
				// 재고 복원 트랜잭션
				for (OrderItemResponse item : order.orderItems()) {
					Product_Stock stock = stockRepository
						.findByProductId(item.productId())
						.orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

					stock.increase(item.quantity());

				}

				// 성공 이벤트 + 멱등성 기록
				sagaIdDeduplication.tryProcess("restore:success:" + request.sagaId(), 600);
				sagaIdDeduplication.remove("processing:" + request.sagaId());

				stockPublisherPort.publishSuccess(
					new StockRestoreSuccessMessage(
						request.sagaId(),
						request.orderId(),
						request.userId()
					)
				);

				log.info("재고 복원 성공 sagaId={}", request.sagaId());

			} catch (Exception ex) {

				sagaIdDeduplication.tryProcess("restore:fail:" + request.sagaId(), 600);
				sagaIdDeduplication.remove("processing:" + request.sagaId());

				stockPublisherPort.publishFail(
					new StockRestoreFailMessage(
						request.sagaId(),
						request.orderId(),
						ex.getMessage(),
						request.userId()
					)
				);

				log.error("재고 복원 실패 sagaId={}", request.sagaId(), ex);
				throw ex;
			} finally {
				// processing 키 제거 (재처리 가능하도록)
				sagaIdDeduplication.remove("processing:" + request.sagaId());
			}
		});
	}
}
