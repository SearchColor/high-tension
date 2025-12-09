package com.high.product.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.high.product.application.dto.external.OrderDetailResponse;
import com.high.product.application.dto.external.OrderItemResponse;
import com.high.product.domain.model.Product_Stock;
import com.high.product.domain.repository.StockRepository;
import com.high.product.infrastructure.client.OrderClient;
import com.high.product.infrastructure.kafka.messaging.failure.StockDeductionFailMessage;
import com.high.product.infrastructure.kafka.messaging.request.StockDeductionCommandRequest;
import com.high.product.infrastructure.kafka.messaging.success.StockDeductionSuccessMessage;
import com.high.product.infrastructure.kafka.producer.ProductKafkaPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockDeductionService {

	private final OrderClient orderClient;
	private final StockRepository stockRepository;
	private final ProductKafkaPublisher kafkaPublisher;

	@Transactional
	public void handleStockDeduction(StockDeductionCommandRequest request) {

		log.info("[StockDeductionService] 재고 차감 요청: sagaId={}, orderId={}", request.sagaId(), request.orderId());

		try {
			// 1. OrderClient 호출: ResponseEntity<ApiResponse<OrderDetailResponse>> 반환 가정
			var responseEntity = orderClient.getOrderdetail(request.orderId());

			// 2. 응답 상태 검증 및 Body 추출
			if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
				throw new IllegalStateException("주문 서비스 응답 실패 또는 본문 없음");
			}

			// 3. ApiResponse에서 실제 데이터 (OrderDetailResponse) 추출
			//    (com.library.module.response.ApiResponse.getData() 호출)
			OrderDetailResponse orderDetailResponse = responseEntity.getBody().data();
			if (orderDetailResponse == null) {
				throw new IllegalStateException("주문 상세 데이터(OrderDetailResponse)가 비어있습니다.");
			}

			// 4. OrderDetailResponse에서 List<OrderItemResponse> (orderItems) 추출
			List<OrderItemResponse> orderItems = orderDetailResponse.orderItems();

			if (orderItems == null || orderItems.isEmpty()) {
				throw new IllegalArgumentException("주문된 상품 목록이 없습니다.");
			}

			// 5. 추출된 상품 목록 리스트(orderItems)를 순회하며 재고 차감
			for(var item : orderItems) {
				// DTO record의 필드를 직접 호출합니다 (item.productId(), item.quantity())
				Product_Stock stock = stockRepository.findById(item.productId())
					.orElseThrow(() -> new IllegalArgumentException("상품 없음: productId=" + item.productId()));

				stock.reduce(item.quantity());
			}

			kafkaPublisher.publishStockDeductionSuccess(
				new StockDeductionSuccessMessage(request.sagaId(), request.orderId(), request.userId())
			);
			log.info("[StockDeductionService] 재고 차감 성공: sagaId={}, orderId={}", request.sagaId(), request.orderId());

		} catch (Exception e) {
			log.error("[StockDeductionService] 재고 차감 실패: sagaId={}, orderId={}, reason={}", request.sagaId(), request.orderId(), e.getMessage());
			kafkaPublisher.publishStockDeductionFail(
				new StockDeductionFailMessage(request.sagaId(), request.orderId(), e.getMessage(), request.userId())
			);
		}
	}
}