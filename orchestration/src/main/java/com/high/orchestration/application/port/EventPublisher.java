package com.high.orchestration.application.port;

import com.high.orchestration.application.dto.internal.request.OrderCreateCommandRequest;

public interface EventPublisher {

    void publishOrderCreateCommand(String topic,
        OrderCreateCommandRequest orderCreateCommandRequest);

}