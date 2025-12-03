package com.high.order.infrastructure.adaptor;

import com.high.order.application.dto.internal.kafka.response.CreateOrderCommand;
import com.high.order.application.dto.internal.kafka.response.CreateOrderItemCommand;
import com.high.order.infrastructure.kafka.dto.response.OrderCreateRequestMessage;
import org.springframework.stereotype.Component;

@Component
public class OrderCreateAdapter {

    public CreateOrderCommand toCommand(OrderCreateRequestMessage message) {
        return new CreateOrderCommand(
            message.couponId(),
            message.sagaId(),
            message.orderId(),
            message.recipient(),
            message.recipientContact(),
            message.deliveryAddress(),
            message.detailAddress(),
            message.requestMessage(),
            message.itemList().stream().map(
                item -> new CreateOrderItemCommand(
                    item.productId(),
                    item.quantity()
                )
            ).toList()
        );

    }
}
