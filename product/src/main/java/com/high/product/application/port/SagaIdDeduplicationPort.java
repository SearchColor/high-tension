package com.high.product.application.port;

public interface SagaIdDeduplicationPort {

	public boolean exists(String key);
	public boolean tryProcess(String sagaId, long ttlSeconds);
}
