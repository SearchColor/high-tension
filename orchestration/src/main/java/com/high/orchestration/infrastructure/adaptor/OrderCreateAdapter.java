package com.high.orchestration.infrastructure.adaptor;

import com.high.orchestration.application.dto.internal.request.ClearCartCommandRequest;
import com.high.orchestration.application.dto.internal.request.CouponUseCommandRequest;
import com.high.orchestration.application.dto.internal.request.OrderDeleteCommandRequest;
import com.high.orchestration.application.dto.internal.request.ProcessOrderSuccessCommandRequest;
import com.high.orchestration.application.dto.internal.request.StockRestoreCommandRequest;
import com.high.orchestration.application.dto.internal.response.OrderCreateFailCommandResponse;
import com.high.orchestration.application.dto.internal.request.PaymentCreateCommandRequest;
import com.high.orchestration.application.dto.internal.request.StockDeductionCommandRequest;
import com.high.orchestration.infrastructure.kafka.dto.response.OrderCreateFailedMessage;
import com.high.orchestration.infrastructure.kafka.dto.response.OrderCreateSuccessMessage;
import com.high.orchestration.infrastructure.kafka.dto.response.PaymentCreateFailMessage;
import com.high.orchestration.infrastructure.kafka.dto.response.PaymentCreateSuccessMessage;
import com.high.orchestration.infrastructure.kafka.dto.response.StockDeductionFailMessage;
import com.high.orchestration.infrastructure.kafka.dto.response.StockDeductionSuccessMessage;
import org.springframework.stereotype.Component;

@Component
public class OrderCreateAdapter {

    public OrderCreateFailCommandResponse toOrderCreateFailCommand(OrderCreateFailedMessage message) {
        return new OrderCreateFailCommandResponse(
            message.sagaId(),
            message.reason()
        );
    }

    public CouponUseCommandRequest toCouponUseCommandRequest(OrderCreateSuccessMessage message) {
        return new CouponUseCommandRequest(
            message.sagaId(),
            message.orderId()
        );

    }

    public StockDeductionCommandRequest toStockDeductionCommand(OrderCreateSuccessMessage message) {
        return new StockDeductionCommandRequest(
            message.sagaId(),
            message.orderId()
        );
    }

    public PaymentCreateCommandRequest toPaymentCreateCommand(StockDeductionSuccessMessage message) {
        return new PaymentCreateCommandRequest(
            message.sagaId(),
            message.orderId()
        );
    }

    public ClearCartCommandRequest toClearCartCommand(PaymentCreateSuccessMessage message) {
        return new ClearCartCommandRequest(
            message.sagaId(),
            message.orderId()
        );
    }

    public ProcessOrderSuccessCommandRequest toProcessOrderSuccessCommand(PaymentCreateSuccessMessage message) {
        return new ProcessOrderSuccessCommandRequest(
            message.sagaId(),
            message.orderId()
        );
    }

    public OrderDeleteCommandRequest toOrderDeleteCommand(StockDeductionFailMessage message) {
        return new OrderDeleteCommandRequest(
            message.sagaId(),
            message.orderId()
        );
    }

    public OrderDeleteCommandRequest toOrderDeleteCommand(PaymentCreateFailMessage message) {
        return new OrderDeleteCommandRequest(
            message.sagaId(),
            message.orderId()
        );
    }

    public StockRestoreCommandRequest toStockRestoreCommand(PaymentCreateFailMessage message) {
        return new StockRestoreCommandRequest(
            message.sagaId(),
            message.orderId()
        );
    }
}
