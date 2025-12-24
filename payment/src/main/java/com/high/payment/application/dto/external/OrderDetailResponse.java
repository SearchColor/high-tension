package com.high.payment.application.dto.external;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;

public record OrderDetailResponse(
	UUID orderId,

	@JsonAlias({"totalPrice", "totalAmount"})
	Integer totalPrice,
	Integer paidAmount
) {


	public Integer totalAmount() {
		return totalPrice;
	}
}
