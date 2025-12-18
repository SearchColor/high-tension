package com.high.product.infrastructure.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RedissonConfig {

	private final RedissonProperties properties;

	@Bean(destroyMethod = "shutdown")
	public RedissonClient redissonClient() {

		Config config = new Config();
		config.setLockWatchdogTimeout(60000);

		// cluster 모드라면 cluster 환경 분산 락
		if ("cluster".equalsIgnoreCase(properties.getMode())) {

			config.useClusterServers()
				.addNodeAddress(
					properties.getCluster()
						.getNodes()
						.toArray(String[]::new)
				)
				.setScanInterval(2000)
				.setTimeout(3000)
				.setRetryAttempts(3)
				.setRetryInterval(1500);

			// 아니라면 단일서버로 동작
		} else {

			config.useSingleServer()
				.setAddress(properties.getSingle().getAddress())
				.setConnectionMinimumIdleSize(5)
				.setConnectionPoolSize(20)
				.setTimeout(3000)
				.setRetryAttempts(3)
				.setRetryInterval(1500);
		}

		return Redisson.create(config);
	}
}
