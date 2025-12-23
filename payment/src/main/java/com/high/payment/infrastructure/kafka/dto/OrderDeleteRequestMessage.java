package com.high.payment.infrastructure.kafka.dto;

import java.util.UUID;

public record OrderDeleteRequestMessage (
	UUID sagaId,
	UUID orderId,
	UUID userId
) {}