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
import com.high.product.application.dto.kafka.failure.StockRestoreFailMessage;
import com.high.product.application.dto.kafka.request.StockRestoreCommandRequest;
import com.high.product.application.dto.kafka.success.StockRestoreSuccessMessage;
import com.high.product.application.service.StockRestoreService;
import com.high.product.application.port.OrderQueryPort;
import com.high.product.application.port.StockPublisherPort;
import com.high.product.domain.model.Product_Stock;
import com.high.product.domain.repository.StockRepository;
import com.library.module.response.ApiResponse;

class StockRestoreServiceTest {

	@InjectMocks
	private StockRestoreService stockRestoreService;

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
	@DisplayName("재고 복원 성공")
	void kafkaRequest_StockRestoreSuccess() {

		UUID sagaId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		UUID productId = UUID.randomUUID();

		StockRestoreCommandRequest request =
			new StockRestoreCommandRequest(sagaId, orderId, userId);

		// 주문 항목 (수량: 2)
		OrderItemResponse orderItem = new OrderItemResponse(
			UUID.randomUUID(),
			productId,
			UUID.randomUUID(),
			1000,
			2,      // quantity
			2000,
			"ORDERED",
			"READY"
		);

		OrderDetailResponse orderDetailResponse = new OrderDetailResponse(
			orderId,
			userId,
			null,
			2000,
			0,
			2000,
			"PAID",
			"홍길동",
			"01012345678",
			"서울",
			"강남구",
			"문앞",
			null,
			List.of(orderItem)
		);

		when(orderQueryPort.getOrderDetail(orderId))
			.thenReturn(ResponseEntity.ok(ApiResponse.success(orderDetailResponse)));

		// ✅ Product_Stock Mock - 생성자 정확히 맞추기
		Product_Stock stock = new Product_Stock(productId, 10);
		when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));

		stockRestoreService.handleStockRestore(request);

		verify(publisherPort).publishSuccess(any(StockRestoreSuccessMessage.class));
		verify(publisherPort, never()).publishFail(any(StockRestoreFailMessage.class));
	}

	@Test
	@DisplayName("재고 복원 실패 - 주문 서비스 호출 오류")
	void kafkaRequest_StockRestoreFail_OrderQueryError() {

		UUID sagaId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();

		StockRestoreCommandRequest request =
			new StockRestoreCommandRequest(sagaId, orderId, userId);

		when(orderQueryPort.getOrderDetail(orderId))
			.thenThrow(new RuntimeException("Order Service 호출 실패"));

		stockRestoreService.handleStockRestore(request);

		verify(publisherPort).publishFail(any(StockRestoreFailMessage.class));
		verify(publisherPort, never()).publishSuccess(any(StockRestoreSuccessMessage.class));
	}

	@Test
	@DisplayName("재고 복원 실패 - Product_Stock 없음")
	void kafkaRequest_StockRestoreFail_ProductNotFound() {

		UUID sagaId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		UUID productId = UUID.randomUUID();

		StockRestoreCommandRequest request =
			new StockRestoreCommandRequest(sagaId, orderId, userId);

		OrderItemResponse item = new OrderItemResponse(
			UUID.randomUUID(),
			productId,
			UUID.randomUUID(),
			1000,
			5,
			5000,
			"ORDERED",
			"READY"
		);

		OrderDetailResponse orderDetailResponse = new OrderDetailResponse(
			orderId,
			userId,
			null,
			2000,
			0,
			2000,
			"PAID",
			"홍길동",
			"01012345678",
			"서울",
			"강남구",
			"문앞",
			null,
			List.of(item)
		);

		when(orderQueryPort.getOrderDetail(orderId))
			.thenReturn(ResponseEntity.ok(ApiResponse.success(orderDetailResponse)));

		when(stockRepository.findById(productId))
			.thenReturn(Optional.empty());

		stockRestoreService.handleStockRestore(request);

		verify(publisherPort).publishFail(any(StockRestoreFailMessage.class));
		verify(publisherPort, never()).publishSuccess(any(StockRestoreSuccessMessage.class));
	}

	@Test
	@DisplayName("재고 복원 실패 - 내부 오류 발생")
	void kafkaRequest_StockRestoreFail_InternalError() {

		UUID sagaId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		UUID productId = UUID.randomUUID();

		StockRestoreCommandRequest request =
			new StockRestoreCommandRequest(sagaId, orderId, userId);

		OrderItemResponse item = new OrderItemResponse(
			UUID.randomUUID(),
			productId,
			UUID.randomUUID(),
			1000,
			3,
			3000,
			"ORDERED",
			"READY"
		);

		OrderDetailResponse orderDetailResponse = new OrderDetailResponse(
			orderId,
			userId,
			null,
			2000,
			0,
			2000,
			"PAID",
			"홍길동",
			"01012345678",
			"서울",
			"강남구",
			"문앞",
			null,
			List.of(item)
		);

		when(orderQueryPort.getOrderDetail(orderId))
			.thenReturn(ResponseEntity.ok(ApiResponse.success(orderDetailResponse)));

		when(stockRepository.findById(productId))
			.thenReturn(Optional.of(new Product_Stock(productId, 10)));

		doThrow(new RuntimeException("DB Update Error"))
			.when(stockRepository).save(any(Product_Stock.class));

		stockRestoreService.handleStockRestore(request);

		verify(publisherPort).publishFail(any(StockRestoreFailMessage.class));
		verify(publisherPort, never()).publishSuccess(any(StockRestoreSuccessMessage.class));
	}
}
