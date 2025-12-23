package com.high.product.application.port;

public interface SagaDeduplicationPort {

	boolean exists(String key);

	boolean tryProcess(String key, long ttlSeconds);

	void save(String key, long ttlSeconds);

	void remove(String key);
}
