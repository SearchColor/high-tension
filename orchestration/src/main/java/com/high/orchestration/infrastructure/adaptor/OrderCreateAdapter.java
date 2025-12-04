package com.high.orchestration.infrastructure.adaptor;

import com.high.orchestration.application.dto.internal.request.StockDeductionCommandRequest;
import com.high.orchestration.infrastructure.kafka.dto.response.OrderCreateSuccessMessage;
import org.springframework.stereotype.Component;

@Component
public class OrderCreateAdapter {

    public StockDeductionCommandRequest toCommand(OrderCreateSuccessMessage message) {
        return new StockDeductionCommandRequest(
            message.orderId(),
            message.sagaId()
        );
    }
}
