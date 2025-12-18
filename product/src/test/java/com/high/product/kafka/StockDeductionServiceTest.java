package com.high.product.kafka;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.high.product.application.dto.external.OrderDetailResponse;
import com.high.product.application.dto.external.OrderItemResponse;
import com.high.product.application.dto.kafka.failure.StockDeductionFailMessage;
import com.high.product.application.dto.kafka.request.StockDeductionCommandRequest;
import com.high.product.application.dto.kafka.success.StockDeductionSuccessMessage;
import com.high.product.application.service.StockDeductionService;
import com.high.product.application.port.OrderQueryPort;
import com.high.product.application.port.StockPublisherPort;
import com.high.product.domain.model.Product_Stock;
import com.high.product.domain.repository.StockRepository;
import com.library.module.response.ApiResponse;

class StockDeductionServiceTest {

	@InjectMocks
	private StockDeductionService stockDeductionService;

	@Mock
	private OrderQueryPort orderQueryPort;

	@Mock
	private StockRepository stockRepository;

	@Mock
	private StockPublisherPort publisherPort;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("재고 차감 성공")
	void kafkaRequest_StockDeductionSuccess() {
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

		when(orderQueryPort.getOrderDetail(orderId))
			.thenReturn(ResponseEntity.ok(ApiResponse.success(orderDetailResponse)));
		when(stockRepository.findById(productId)).thenReturn(Optional.of(new Product_Stock(productId, 10)));

		stockDeductionService.handleStockDeduction(request);

		verify(publisherPort).publishSuccess(any(StockDeductionSuccessMessage.class));
		verify(publisherPort, never()).publishFail(any(StockDeductionFailMessage.class));
	}

	@Test
	@DisplayName("재고 차감 실패 - 재고 부족")
	void kafkaRequest_StockDeductionFail_InsufficientStock() {
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

		when(orderQueryPort.getOrderDetail(orderId))
			.thenReturn(ResponseEntity.ok(ApiResponse.success(orderDetailResponse)));
		when(stockRepository.findById(productId)).thenReturn(Optional.of(new Product_Stock(productId, 10)));

		stockDeductionService.handleStockDeduction(request);

		verify(publisherPort).publishFail(any(StockDeductionFailMessage.class));
		verify(publisherPort, never()).publishSuccess(any(StockDeductionSuccessMessage.class));
	}

	@Test
	@DisplayName("재고 차감 실패 - 주문 서비스 호출 실패")
	void kafkaRequest_StockDeductionFail_OrderClientError() {
		UUID sagaId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();

		StockDeductionCommandRequest request = new StockDeductionCommandRequest(sagaId, orderId, userId);

		when(orderQueryPort.getOrderDetail(orderId)).thenThrow(new RuntimeException("Order Service 호출 실패"));

		stockDeductionService.handleStockDeduction(request);

		verify(publisherPort).publishFail(any(StockDeductionFailMessage.class));
		verify(publisherPort, never()).publishSuccess(any(StockDeductionSuccessMessage.class));
	}
}
