package com.high.payment.infrastructure.config;

import org.springframework.context.annotation.Configuration;

import com.high.payment.infrastructure.context.MessageContext;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FeignConfig implements RequestInterceptor {

	@Override
	public void apply(RequestTemplate requestTemplate) {
		// 1. Kafka 메시지 기반 Context 우선
		if (MessageContext.hasContext()) {
			requestTemplate.header("X-User-Id", MessageContext.getUserId().toString());
			requestTemplate.header("X-User-Role", MessageContext.getRole());
			return;
		}

		log.debug("[Fegin] No MessageContext found. Skip header injection." );
	}
}
