package com.high.orchestration.application.port;

import com.high.orchestration.application.dto.internal.request.ClearCartCommandRequest;
import com.high.orchestration.application.dto.internal.request.CouponUseCommandRequest;
import com.high.orchestration.application.dto.internal.request.OrderCreateCommandRequest;
import com.high.orchestration.application.dto.internal.request.OrderDeleteCommandRequest;
import com.high.orchestration.application.dto.internal.request.PaymentCreateCommandRequest;
import com.high.orchestration.application.dto.internal.request.ProcessOrderSuccessCommandRequest;
import com.high.orchestration.application.dto.internal.request.StockDeductionCommandRequest;
import com.high.orchestration.application.dto.internal.request.StockRestoreCommandRequest;

public interface EventPublisher {

    void publishOrderCreateCommand(String topic,
        OrderCreateCommandRequest orderCreateCommandRequest);

    void publishStockDeductionCommand(String topic, StockDeductionCommandRequest message);

    void publishPaymentCreateCommand(String topic, PaymentCreateCommandRequest request);

    void publishClearCartCommand(String topic, ClearCartCommandRequest request);

    void publishCouponUseCommand(String topic, CouponUseCommandRequest commandRequest);

    void publishOrderDeleteCommand(String topic, OrderDeleteCommandRequest request);

    void publishOrderSuccessProcessingCommand(String topic, ProcessOrderSuccessCommandRequest orderRequest);

    void publishStockRestoreCommand(String topic, StockRestoreCommandRequest stockRequest);
}