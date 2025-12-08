package com.high.order.application.port;

import com.high.order.application.dto.internal.kafka.response.OrderCreateFailedResponse;
import com.high.order.application.dto.internal.kafka.response.OrderSuccessResponse;

public interface EventPublisher {
    void sendOrderCreateSuccess(String topic, OrderSuccessResponse response);
    void sendOrderCreateFail(String topic, OrderCreateFailedResponse orderCreateFailedResponse);

}
