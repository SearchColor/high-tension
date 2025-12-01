package com.high.order.presentation;

import com.high.order.application.dto.request.OrderCreateRequest;
import com.high.order.application.dto.response.OrderCreateResponse;
import com.high.order.application.dto.response.OrderDetailResponse;
import com.high.order.application.service.OrderService;
import com.library.module.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/product")
    public ApiResponse<OrderCreateResponse> createOrder(@Valid @RequestBody OrderCreateRequest productOrderCreateRequest) {
        OrderCreateResponse response = orderService.createOrder(productOrderCreateRequest);
        return ApiResponse.success(response);
    }

    @PostMapping("/limited-product")
    public ResponseEntity createLimitedProductOrder() {
        return ResponseEntity.ok().build();
    }


    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponse> getOrderDetail(@PathVariable("orderId") UUID orderId) {

        OrderDetailResponse response = orderService.getOrderDetail(orderId);
        return ApiResponse.success(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderDetailResponse>> getOrders() {
        List<OrderDetailResponse> orderDetailResponseList = new ArrayList<>();
        return ResponseEntity.ok(orderDetailResponseList);
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity cancelOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{orderId}")
    public ResponseEntity changeOrderStatus(@PathVariable UUID orderId) {
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{orderId}")
    public ApiResponse<Void> deleteOrder(@PathVariable UUID orderId) {
        orderService.deleteOrder(orderId);
        return ApiResponse.success("삭제되었습니다.");
    }

}
