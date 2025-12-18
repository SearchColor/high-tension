package com.high.product.infrastructure.redis;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SagaIdDeduplication {

	private final RedissonClient redissonClient;

	// 이미 존재하는지 체크
	public boolean exists(String key) {
		return Boolean.TRUE.equals(redissonClient.getBucket(key).get());
	}

	// 최초 처리 시도
	public boolean tryProcess(String key, long ttlSeconds) {
		RBucket<Boolean> bucket = redissonClient.getBucket(key);
		Boolean exists = bucket.get();
		if (exists != null && exists) return false;
		bucket.set(true, ttlSeconds, TimeUnit.SECONDS);
		return true;
	}

	// processing key 삭제 (재처리 가능)
	public void remove(String key) {
		redissonClient.getBucket(key).delete();
	}
}
