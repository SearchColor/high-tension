package com.high.product.kafka;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.high.product.application.dto.external.OrderDetailResponse;
import com.high.product.application.dto.external.OrderItemResponse;
import com.high.product.application.service.StockDeductionService;
import com.high.product.domain.model.Product_Stock;
import com.high.product.domain.repository.StockRepository;
import com.high.product.infrastructure.client.OrderClient;
import com.high.product.infrastructure.kafka.messaging.failure.StockDeductionFailMessage;
import com.high.product.infrastructure.kafka.messaging.request.StockDeductionCommandRequest;
import com.high.product.infrastructure.kafka.messaging.success.StockDeductionSuccessMessage;
import com.high.product.infrastructure.kafka.producer.ProductKafkaPublisher;
import com.library.module.response.ApiResponse;


class StockDeductionServiceTest {

	@InjectMocks
	private StockDeductionService stockDeductionService;

	@Mock
	private OrderClient orderClient;

	@Mock
	private StockRepository stockRepository;

	@Mock
	private ProductKafkaPublisher kafkaPublisher;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("재고 차감 성공")
	void kafkaRequest_StockDeductionSuccess() {
		// given
		UUID sagaId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		UUID productId = UUID.randomUUID();

		StockDeductionCommandRequest request = new StockDeductionCommandRequest(sagaId, orderId, userId);

		OrderItemResponse orderItem = new OrderItemResponse(UUID.randomUUID(), productId, UUID.randomUUID(),
			1000, 2, 2000, "ORDERED", "READY");
		OrderDetailResponse orderDetailResponse = new OrderDetailResponse(
			orderId, userId, null, 2000, 0, 2000, "PAID",
			"홍길동", "01012345678", "서울", "강남구", "문앞",
			null, List.of(orderItem)
		);

		ApiResponse<OrderDetailResponse> apiResponse =
			ApiResponse.success(orderDetailResponse);

		when(orderClient.getOrderdetail(orderId)).thenReturn(ResponseEntity.ok(apiResponse));
		when(stockRepository.findById(productId)).thenReturn(Optional.of(new Product_Stock(productId, 10)));

		// when
		stockDeductionService.handleStockDeduction(request);


		// then

		verify(kafkaPublisher).publishStockDeductionSuccess(any(StockDeductionSuccessMessage.class));
		verify(kafkaPublisher, never()).publishStockDeductionFail(any(StockDeductionFailMessage.class));

		System.out.println("[Test] 재고 차감 성공: sagaId=" + request.sagaId()
			+ ", orderId=" + request.orderId()
			+ ", userId=" + request.userId());
	}

	@Test
	@DisplayName("재고 차감 실패")
	void kafkaRequest_StockDeductionFail_InsufficientStock() {
		// given
		UUID sagaId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		UUID productId = UUID.randomUUID();

		StockDeductionCommandRequest request = new StockDeductionCommandRequest(sagaId, orderId, userId);

		OrderItemResponse orderItem = new OrderItemResponse(UUID.randomUUID(), productId, UUID.randomUUID(),
			1000, 20, 20000, "ORDERED", "READY"); // 재고 부족
		OrderDetailResponse orderDetailResponse = new OrderDetailResponse(
			orderId, userId, null, 20000, 0, 20000, "PAID",
			"홍길동", "01012345678", "서울", "강남구", "문앞",
			null, List.of(orderItem)
		);

		ApiResponse<OrderDetailResponse> apiResponse =
			ApiResponse.success(orderDetailResponse, "주문 조회 성공");

		when(orderClient.getOrderdetail(orderId)).thenReturn(ResponseEntity.ok(apiResponse));
		when(stockRepository.findById(productId)).thenReturn(Optional.of(new Product_Stock(productId, 10)));

		// when
		stockDeductionService.handleStockDeduction(request);

		// then
		verify(kafkaPublisher).publishStockDeductionFail(any(StockDeductionFailMessage.class));
		verify(kafkaPublisher, never()).publishStockDeductionSuccess(any(StockDeductionSuccessMessage.class));
	}

	@Test
	@DisplayName("재고 차감 실패2")
	void kafkaRequest_StockDeductionFail_OrderClientError() {
		// given
		UUID sagaId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();

		StockDeductionCommandRequest request = new StockDeductionCommandRequest(sagaId, orderId, userId);

		when(orderClient.getOrderdetail(orderId)).thenThrow(new RuntimeException("Order Service 호출 실패"));

		// when
		stockDeductionService.handleStockDeduction(request);

		// then
		verify(kafkaPublisher).publishStockDeductionFail(any(StockDeductionFailMessage.class));
		verify(kafkaPublisher, never()).publishStockDeductionSuccess(any(StockDeductionSuccessMessage.class));
	}
}
