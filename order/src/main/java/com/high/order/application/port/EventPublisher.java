package com.high.order.application.port;

import com.high.order.application.dto.internal.kafka.request.ProcessOrderSuccessCommand;
import com.high.order.application.dto.internal.kafka.response.OrderCreateFailedResponse;
import com.high.order.application.dto.internal.kafka.response.OrderDeleteResponse;
import com.high.order.application.dto.internal.kafka.response.OrderSuccessResponse;

public interface EventPublisher {
    void sendOrderCreateSuccess(String topic, OrderSuccessResponse response);

    void sendOrderCreateFail(String topic, OrderCreateFailedResponse orderCreateFailedResponse);

    void sendOrderDeleteSuccess(String topic, OrderDeleteResponse response);

    void sendOrderDeleteFail(String topic, OrderDeleteResponse response);

    void sendOrderProcessSuccess(String topic, ProcessOrderSuccessCommand command);
}
