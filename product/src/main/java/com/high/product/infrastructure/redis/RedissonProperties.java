package com.high.product.infrastructure.redis;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "redisson")
public class RedissonProperties {

	private String mode;

	private Single single = new Single();
	private Cluster cluster = new Cluster();

	@Getter @Setter
	public static class Single {
		private String address;
	}

	@Getter @Setter
	public static class Cluster {
		private List<String> nodes;
	}
}
