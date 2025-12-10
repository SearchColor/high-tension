package com.high.payment.domain.port.out;

import java.util.UUID;

public interface UserValidationPort {

	void validateUser(UUID userId);
}
