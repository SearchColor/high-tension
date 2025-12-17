package com.high.product.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.high.product.application.dto.external.OrderDetailResponse;
import com.high.product.application.dto.external.OrderItemResponse;
import com.high.product.application.dto.kafka.failure.StockRestoreFailMessage;
import com.high.product.application.dto.kafka.request.StockRestoreCommandRequest;
import com.high.product.application.dto.kafka.success.StockRestoreSuccessMessage;
import com.high.product.application.port.OrderQueryPort;
import com.high.product.application.port.StockPublisherPort;
import com.high.product.domain.model.Product_Stock;
import com.high.product.domain.repository.StockRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockRestoreService {

	private final OrderQueryPort orderQueryPort;
	private final StockRepository stockRepository;
	private final StockPublisherPort publisherPort;

	@Transactional
	public void handleStockRestore(StockRestoreCommandRequest request) {

		log.info("[StockRestoreService] 재고 복원 요청: sagaId={}, orderId={}", request.sagaId(), request.orderId());

		try {
			// 1. Order 조회
			var responseEntity = orderQueryPort.getOrderDetail(request.orderId());
			if (responseEntity == null || responseEntity.getBody() == null) {
				throw new IllegalStateException("주문 상세 데이터 조회 실패: orderId=" + request.orderId());
			}
			OrderDetailResponse orderDetailResponse = responseEntity.getBody().data();
			if (orderDetailResponse == null) {
				throw new IllegalStateException("주문 상세 데이터(OrderDetailResponse)가 비어있습니다.");
			}

			List<OrderItemResponse> orderItems = orderDetailResponse.orderItems();
			if (orderItems == null || orderItems.isEmpty()) {
				throw new IllegalArgumentException("주문된 상품 목록이 없습니다.");
			}

			// 2. 재고 복원 (DB 트랜잭션 안에서)
			for (var item : orderItems) {
				Product_Stock stock = stockRepository.findByProductId(item.productId())
					.orElseThrow(() -> new IllegalArgumentException("상품 없음: productId=" + item.productId()));
				stock.increase(item.quantity());
			}

			// 3. 성공 이벤트 발행
			publisherPort.publishSuccess(
				new StockRestoreSuccessMessage(request.sagaId(), request.orderId(), request.userId())
			);

			log.info("[StockRestoreService] 재고 복원 성공: sagaId={}, orderId={}", request.sagaId(), request.orderId());

		} catch (Exception e) {
			log.error("[StockRestoreService] 재고 복원 실패: sagaId={}, orderId={}, reason={}",
				request.sagaId(), request.orderId(), e.getMessage(), e);

			// 4. 실패 이벤트 발행
			publisherPort.publishFail(
				new StockRestoreFailMessage(request.sagaId(), request.orderId(), e.getMessage(), request.userId())
			);
		}
	}
}