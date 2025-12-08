package com.high.orchestration.application.port;

import com.high.orchestration.application.dto.internal.request.ClearCartCommandRequest;
import com.high.orchestration.application.dto.internal.request.CouponUseCommandRequest;
import com.high.orchestration.application.dto.internal.request.OrderCreateCommandRequest;
import com.high.orchestration.application.dto.internal.request.PaymentCreateCommandRequest;
import com.high.orchestration.application.dto.internal.request.StockDeductionCommandRequest;

public interface EventPublisher {

    void publishOrderCreateCommand(String topic,
        OrderCreateCommandRequest orderCreateCommandRequest);

    void publishStockDeductionCommand(String topic, StockDeductionCommandRequest message);

    void publishPaymentCreateCommand(String s, PaymentCreateCommandRequest request);

    void publishClearCartCommand(String s, ClearCartCommandRequest request);

    void publishCouponUseCommand(String s, CouponUseCommandRequest commandRequest);
}