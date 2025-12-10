package com.high.product.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.high.product.application.dto.external.OrderDetailResponse;
import com.high.product.application.dto.external.OrderItemResponse;
import com.high.product.application.dto.kafka.failure.StockDeductionFailMessage;
import com.high.product.application.dto.kafka.request.StockDeductionCommandRequest;
import com.high.product.application.dto.kafka.success.StockDeductionSuccessMessage;
import com.high.product.application.port.OrderQueryPort;
import com.high.product.application.port.StockDeductionPublisherPort;
import com.high.product.domain.model.Product_Stock;
import com.high.product.domain.repository.StockRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockDeductionService {

	private final OrderQueryPort orderQueryPort;
	private final StockRepository stockRepository;
	private final StockDeductionPublisherPort publisherPort;

	@Transactional
	public void handleStockDeduction(StockDeductionCommandRequest request) {

		log.info("[StockDeductionService] 재고 차감 요청: sagaId={}, orderId={}", request.sagaId(), request.orderId());

		try {
			// 1. Port 호출 (infrastructure 구현체를 직접 호출하지 않음)

			var responseEntity = orderQueryPort.getOrderDetail(request.orderId());
			assert responseEntity.getBody() != null;
			OrderDetailResponse orderDetailResponse = responseEntity.getBody().data();
			if (orderDetailResponse == null) {
				throw new IllegalStateException("주문 상세 데이터(OrderDetailResponse)가 비어있습니다.");
			}

			List<OrderItemResponse> orderItems = orderDetailResponse.orderItems();
			if (orderItems == null || orderItems.isEmpty()) {
				throw new IllegalArgumentException("주문된 상품 목록이 없습니다.");
			}

			// 2. 재고 차감
			for(var item : orderItems) {
				Product_Stock stock = stockRepository.findById(item.productId())
					.orElseThrow(() -> new IllegalArgumentException("상품 없음: productId=" + item.productId()));
				stock.reduce(item.quantity());
			}

			// 3. 성공 메시지 발행 (Port 통해)
			publisherPort.publishSuccess(
				new StockDeductionSuccessMessage(request.sagaId(), request.orderId(), request.userId())
			);

			log.info("[StockDeductionService] 재고 차감 성공: sagaId={}, orderId={}", request.sagaId(), request.orderId());

		} catch (Exception e) {
			log.error("[StockDeductionService] 재고 차감 실패: sagaId={}, orderId={}, reason={}",
				request.sagaId(), request.orderId(), e.getMessage());

			// 4. 실패 메시지 발행 (Port 통해)
			publisherPort.publishFail(
				new StockDeductionFailMessage(request.sagaId(), request.orderId(), e.getMessage(), request.userId())
			);
		}
	}
}