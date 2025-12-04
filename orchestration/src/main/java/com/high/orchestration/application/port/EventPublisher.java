package com.high.orchestration.application.port;

import com.high.orchestration.application.dto.internal.request.OrderCreateCommandRequest;
import com.high.orchestration.application.dto.internal.request.StockDeductionCommandRequest;

public interface EventPublisher {

    void publishOrderCreateCommand(String topic,
        OrderCreateCommandRequest orderCreateCommandRequest);

    void publishStockDeductionCommand(String topic, StockDeductionCommandRequest message);
}