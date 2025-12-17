package com.high.product.application.port;

import com.high.product.application.dto.kafka.failure.StockDeductionFailMessage;
import com.high.product.application.dto.kafka.failure.StockRestoreFailMessage;
import com.high.product.application.dto.kafka.success.StockDeductionSuccessMessage;
import com.high.product.application.dto.kafka.success.StockRestoreSuccessMessage;

public interface StockPublisherPort {
	void publishSuccess(StockDeductionSuccessMessage message);
	void publishFail(StockDeductionFailMessage message);

	void publishSuccess(StockRestoreSuccessMessage message);
	void publishFail(StockRestoreFailMessage message);
}