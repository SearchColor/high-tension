package com.high.product.application.port;

import java.util.List;

public interface DistributedLockPort {

	void executeWithMultiLock(
		List<String> lockKeys,
		Runnable action
	);
}
