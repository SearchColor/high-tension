package com.high.order.presentation;

import com.high.order.application.OrderService;
import com.high.order.application.dto.request.OrderCreateRequest;
import com.high.order.application.dto.request.OrderItemDeliveryStatusChangeRequest;
import com.high.order.application.dto.request.OrderItemStatusChangeRequest;
import com.high.order.application.dto.request.OrderStatusChangeRequest;
import com.high.order.application.dto.request.OrderUpdateRequest;
import com.high.order.application.dto.response.OrderDetailResponse;
import com.high.order.application.dto.response.OrderItemIdResponse;
import com.high.order.application.dto.response.OrderListResponse;
import com.high.order.application.dto.response.OrderResponse;
import com.library.module.response.ApiResponse;
import com.library.security.util.SecurityContextUtil;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/product")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody OrderCreateRequest productOrderCreateRequest
       ) {
        UUID userId = SecurityContextUtil.getCurrentUserId();
        String userRole = SecurityContextUtil.getCurrentUserRole();
        log.info("userId: {}, userRole: {}", userId, userRole);
        OrderResponse response = orderService.createOrder(productOrderCreateRequest, userId, userRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PreAuthorize("hasAnyRole('USER', 'MASTER')")
    @PostMapping("/limited-product")
    public ResponseEntity createLimitedProductOrder() {
        return ResponseEntity.ok().build();
    }


    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(@PathVariable("orderId") UUID orderId) {
        UUID userId = SecurityContextUtil.getCurrentUserId();
        String userRole = SecurityContextUtil.getCurrentUserRole();
        OrderDetailResponse response = orderService.getOrderDetail(orderId, userId, userRole);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderListResponse>>> getOrders() {
        UUID userId = SecurityContextUtil.getCurrentUserId();
        String userRole = SecurityContextUtil.getCurrentUserRole();
        List<OrderListResponse> responses = orderService.getOrders(userId, userRole);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responses));
    }


    //전체 취소
    @PreAuthorize("hasAnyRole('USER','MASTER')")
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable UUID orderId) {
        UUID userId = SecurityContextUtil.getCurrentUserId();
        String userRole = SecurityContextUtil.getCurrentUserRole();
        OrderResponse response = orderService.cancelOrder(orderId, userId, userRole);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    //부분 취소
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @PatchMapping("/{orderId}/cancel/{orderItemId}")
    public ResponseEntity<ApiResponse<OrderItemIdResponse>> cancelOrderItem(@PathVariable UUID orderId, @PathVariable UUID orderItemId) {
        UUID userId = SecurityContextUtil.getCurrentUserId();
        String userRole = SecurityContextUtil.getCurrentUserRole();
        OrderItemIdResponse response = orderService.cancelOrderItem(orderId, orderItemId, userId, userRole);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    //주문 정보 변경
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @PatchMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(@PathVariable UUID orderId, @Valid @RequestBody OrderUpdateRequest request) {
        UUID userId = SecurityContextUtil.getCurrentUserId();
        String userRole = SecurityContextUtil.getCurrentUserRole();
        OrderResponse response = orderService.updateOrder(orderId, request, userId, userRole);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    //주문 상태 변경
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> changeOrderStatus(@PathVariable UUID orderId, @RequestBody
    OrderStatusChangeRequest request) {
        UUID userId = SecurityContextUtil.getCurrentUserId();
        String userRole = SecurityContextUtil.getCurrentUserRole();
        OrderResponse response = orderService.changeOrderStatus(orderId, request, userId, userRole);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    //주문 아이템 환불 상태 변경
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'MASTER')")
    @PatchMapping("/{orderId}/items/{orderItemId}/status")
    public ResponseEntity<ApiResponse<OrderItemIdResponse>> changeOrderItemStatus(@PathVariable UUID orderId, @PathVariable UUID orderItemId,
                                            @RequestBody @Valid OrderItemStatusChangeRequest request) {
        UUID userId = SecurityContextUtil.getCurrentUserId();
        String userRole = SecurityContextUtil.getCurrentUserRole();
        OrderItemIdResponse response = orderService.changeOrderItemStatus(orderId, orderItemId, request, userId, userRole);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    //주문 아이템 배송 상태 변경
    @PreAuthorize("hasAnyRole('SELLER', 'MASTER')")

    @PatchMapping("/{orderId}/items/{orderItemId}/delivery-status")
    public ResponseEntity<ApiResponse<OrderItemIdResponse>> changeOrderItemDeliveryStatus(
                        @PathVariable UUID orderId, @PathVariable UUID orderItemId, @RequestBody @Valid OrderItemDeliveryStatusChangeRequest request) {
        UUID userId = SecurityContextUtil.getCurrentUserId();
        String userRole = SecurityContextUtil.getCurrentUserRole();
        OrderItemIdResponse response = orderService.changeOrderItemDeliveryStatus(orderId, orderItemId, request, userId, userRole);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @PreAuthorize("hasAnyRole('USER', 'MASTER')")
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable UUID orderId) {
        UUID userId = SecurityContextUtil.getCurrentUserId();
        String userRole = SecurityContextUtil.getCurrentUserRole();
        orderService.deleteOrder(orderId, userId, userRole);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("삭제되었습니다."));
    }

}
