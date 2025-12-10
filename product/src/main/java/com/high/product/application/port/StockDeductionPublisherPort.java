package com.high.product.application.port;

import com.high.product.application.dto.kafka.failure.StockDeductionFailMessage;
import com.high.product.application.dto.kafka.success.StockDeductionSuccessMessage;

public interface StockDeductionPublisherPort {
	void publishSuccess(StockDeductionSuccessMessage message);
	void publishFail(StockDeductionFailMessage message);
}