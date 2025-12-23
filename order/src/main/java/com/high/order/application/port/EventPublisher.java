package com.high.order.application.port;

import com.high.order.application.dto.event.request.ProcessOrderSuccessCommand;
import com.high.order.application.dto.event.response.OrderCreateFailedResponse;
import com.high.order.application.dto.event.response.OrderDeleteResponse;
import com.high.order.application.dto.event.response.OrderSuccessResponse;

public interface EventPublisher {
    void sendOrderCreateSuccess(String topic, OrderSuccessResponse response);

    void sendOrderCreateFail(String topic, OrderCreateFailedResponse orderCreateFailedResponse);

    void sendOrderDeleteSuccess(String topic, OrderDeleteResponse response);

    void sendOrderDeleteFail(String topic, OrderDeleteResponse response);

    void sendOrderProcessSuccess(String topic, ProcessOrderSuccessCommand command);
}
