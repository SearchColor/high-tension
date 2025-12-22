package com.high.order.infrastructure.kafka.adapter;

import com.high.order.application.dto.event.request.CreateOrderCommand;
import com.high.order.application.dto.event.request.CreateOrderItemCommand;
import com.high.order.application.dto.event.request.DeleteOrderCommand;
import com.high.order.infrastructure.kafka.dto.response.OrderCreateRequestMessage;
import com.high.order.infrastructure.kafka.dto.response.OrderDeleteRequestMessage;
import org.springframework.stereotype.Component;

@Component
public class OrderCreateAdapter {

    public CreateOrderCommand toCreateCommand(OrderCreateRequestMessage message) {
        return new CreateOrderCommand(
            message.couponIssueId(),
            message.sagaId(),
            message.orderId(),
            message.ordererId(),
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

    public DeleteOrderCommand toDeleteCommand(OrderDeleteRequestMessage message) {
        return new DeleteOrderCommand(
            message.sagaId(),
            message.orderId(),
            message.userId()
        );
    }

}
