package com.high.order.application.dto.response;

import com.high.order.domain.entity.OrderItem;
import java.util.UUID;

public record OrderItemResponse(
    UUID orderItemId,
    UUID productId,
    UUID producerId,
    Integer unitPrice,
    Integer quantity,
    Integer totalPrice,
    String orderItemStatus,
    String deliveryStatus
) {
    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
            orderItem.getOrderItemId(),
            orderItem.getProductId(),
            orderItem.getProducerId(),
            orderItem.getUnitPrice(),
            orderItem.getQuantity(),
            orderItem.getItemTotalPrice(),
            orderItem.getOrderItemStatus().toString(),
            orderItem.getDeliveryStatus().toString()
        );
    }
}
