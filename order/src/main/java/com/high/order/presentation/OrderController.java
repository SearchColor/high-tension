package com.high.order.presentation;

import com.high.order.application.dto.response.OrderDetailResponse;
import com.high.order.application.dto.response.OrderItemResponse;
import com.high.order.domain.vo.DeliveryStatus;
import com.high.order.domain.vo.OrderItemStatus;
import com.high.order.domain.vo.OrderStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @PostMapping("/cart")
    public ResponseEntity createOrderFromCart() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/product")
    public ResponseEntity createOrderFromProduct() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/limited-product")
    public ResponseEntity createOrderFromLimitedProduct() {
        return ResponseEntity.ok().build();
    }


    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable("orderId") UUID orderId) {

        //임시 더미데이터
        String uuidString = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
        OrderItemResponse orderItemResponse1 = new OrderItemResponse(
            UUID.fromString(uuidString),
            UUID.randomUUID(),
            UUID.randomUUID(),
            2000,
            10,
            20000,
            OrderItemStatus.CREATED.toString(),
            DeliveryStatus.READY.toString()
        );

        OrderItemResponse orderItemResponse2 = new OrderItemResponse(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            1000,
            10,
            10000,
            OrderItemStatus.CREATED.toString(),
            DeliveryStatus.READY.toString()
        );

        List<OrderItemResponse> orderItemList = new ArrayList<>();
        orderItemList.add(orderItemResponse1);
        orderItemList.add(orderItemResponse2);

        OrderDetailResponse orderDetailResponse = new OrderDetailResponse(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            30000,
            3000,
            27000,
            OrderStatus.CREATED.toString(),
            "홍길동",
            "010-1234-5678",
            "서울특별시 강남구 역삼동 227-1",
            "401호",
            "문앞에 두고 가주세요",
            LocalDateTime.now(),
            orderItemList

        );
        return ResponseEntity.ok(orderDetailResponse);
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
    public ResponseEntity deleteOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok().build();
    }

}
