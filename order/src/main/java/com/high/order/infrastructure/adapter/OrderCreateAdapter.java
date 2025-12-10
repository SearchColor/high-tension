package com.high.order.infrastructure.adapter;

import com.high.order.application.dto.internal.kafka.request.CreateOrderCommand;
import com.high.order.application.dto.internal.kafka.request.CreateOrderItemCommand;
import com.high.order.application.dto.internal.kafka.request.DeleteOrderCommand;
import com.high.order.application.dto.internal.kafka.request.ProcessOrderSuccessCommand;
import com.high.order.infrastructure.kafka.dto.response.OrderCreateRequestMessage;
import com.high.order.infrastructure.kafka.dto.response.OrderDeleteRequestMessage;
import com.high.order.infrastructure.kafka.dto.response.OrderProcessSuccessMessage;
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
            message.orderId()
        );
    }

    public ProcessOrderSuccessCommand toProcessCommand(OrderProcessSuccessMessage message) {
        return new ProcessOrderSuccessCommand(
            message.sagaId(),
            message.orderId()
        );
    }
}
