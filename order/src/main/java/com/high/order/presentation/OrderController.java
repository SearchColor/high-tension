package com.high.order.presentation;

import com.high.order.application.dto.request.OrderCreateRequest;
import com.high.order.application.dto.request.OrderItemDeliveryStatusChangeRequest;
import com.high.order.application.dto.request.OrderItemStatusChangeRequest;
import com.high.order.application.dto.request.OrderStatusChangeRequest;
import com.high.order.application.dto.response.OrderDetailResponse;
import com.high.order.application.dto.response.OrderItemIdResponse;
import com.high.order.application.dto.response.OrderListResponse;
import com.high.order.application.dto.response.OrderResponse;
import com.high.order.application.service.OrderService;
import com.library.module.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/product")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody OrderCreateRequest productOrderCreateRequest) {
        OrderResponse response = orderService.createOrder(productOrderCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/limited-product")
    public ResponseEntity createLimitedProductOrder() {
        return ResponseEntity.ok().build();
    }


    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(@PathVariable("orderId") UUID orderId) {
        OrderDetailResponse response = orderService.getOrderDetail(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderListResponse>>> getOrders() {
        List<OrderListResponse> responses = orderService.getOrders();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responses));
    }


    //전체 취소
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable UUID orderId) {
        OrderResponse response = orderService.cancelOrder(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    //부분 취소
    @PatchMapping("/{orderId}/cancel/{orderItemId}")
    public ResponseEntity<ApiResponse<OrderItemIdResponse>> cancelOrderItem(@PathVariable UUID orderId, @PathVariable UUID orderItemId) {
        OrderItemIdResponse response = orderService.cancelOrderItem(orderId, orderItemId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    //주문 상태 변경
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> changeOrderStatus(@PathVariable UUID orderId, @RequestBody
    OrderStatusChangeRequest request) {
        OrderResponse response = orderService.changeOrderStatus(orderId, request);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    //주문 아이템 환불 상태 변경
    @PatchMapping("/{orderId}/items/{orderItemId}/status")
    public ResponseEntity<ApiResponse<OrderItemIdResponse>> changeOrderItemStatus(@PathVariable UUID orderId, @PathVariable UUID orderItemId,
                                            @RequestBody @Valid OrderItemStatusChangeRequest request) {
        OrderItemIdResponse response = orderService.changeOrderItemStatus(orderId, orderItemId, request);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @PatchMapping("/{orderId}/items/{orderItemId}/delivery-status")
    public ResponseEntity<ApiResponse<OrderItemIdResponse>> changeOrderItemDeliveryStatus(
                        @PathVariable UUID orderId, @PathVariable UUID orderItemId, @RequestBody @Valid OrderItemDeliveryStatusChangeRequest request) {
        log.info("진입");
        OrderItemIdResponse response = orderService.changeOrderItemDeliveryStatus(orderId, orderItemId, request);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable UUID orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("삭제되었습니다."));
    }

}
