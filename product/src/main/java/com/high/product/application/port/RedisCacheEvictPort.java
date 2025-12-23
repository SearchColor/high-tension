package com.high.product.application.port;

import java.util.UUID;

public interface RedisCacheEvictPort {
	void evictStockCacheAfterCommit(UUID productId);
}
