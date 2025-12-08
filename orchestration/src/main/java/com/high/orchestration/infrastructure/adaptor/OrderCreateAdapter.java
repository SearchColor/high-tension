package com.high.orchestration.infrastructure.adaptor;

import com.high.orchestration.application.dto.internal.request.ClearCartCommandRequest;
import com.high.orchestration.application.dto.internal.request.PaymentCreateCommandRequest;
import com.high.orchestration.application.dto.internal.request.StockDeductionCommandRequest;
import com.high.orchestration.infrastructure.kafka.dto.response.OrderCreateSuccessMessage;
import com.high.orchestration.infrastructure.kafka.dto.response.PaymentCreateSuccessMessage;
import com.high.orchestration.infrastructure.kafka.dto.response.StockDeductionSuccessMessage;
import org.springframework.stereotype.Component;

@Component
public class OrderCreateAdapter {

    public StockDeductionCommandRequest toStockDeductionCommand(OrderCreateSuccessMessage message) {
        return new StockDeductionCommandRequest(
            message.orderId(),
            message.sagaId()
        );
    }

    public PaymentCreateCommandRequest toPaymentCreateCommand(StockDeductionSuccessMessage message) {
        return new PaymentCreateCommandRequest(
            message.orderId(),
            message.sagaId()
        );
    }

    public ClearCartCommandRequest toClearCartCommand(PaymentCreateSuccessMessage message) {
        return new ClearCartCommandRequest(
            message.orderId(),
            message.sagaId()
        );
    }
}
