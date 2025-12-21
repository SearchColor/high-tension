package com.high.product.infrastructure.redis;

import java.util.UUID;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.high.product.application.port.RedisCacheEvictPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisCacheEvictAdapter implements RedisCacheEvictPort {

	private static final String STOCK_CACHE = "stock";

	private final CacheManager cacheManager;

	@Override
	public void evictStockCacheAfterCommit(UUID productId) {

		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			evict(productId);
			return;
		}

		TransactionSynchronizationManager.registerSynchronization(
			new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					evict(productId);
				}
			}
		);
	}

	private void evict(UUID productId) {
		if (cacheManager.getCache(STOCK_CACHE) != null) {
			cacheManager.getCache(STOCK_CACHE).evict(productId);
		}
	}
}
