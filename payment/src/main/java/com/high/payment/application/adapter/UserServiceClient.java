package com.high.payment.application.adapter;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name ="user-service", url = "${user-service.url}")
public interface UserServiceClient {
	// 유저 유효성 검증 API 호출을 위한 메서드 정의
	@GetMapping("/users/validate/{userId}")
	void validateUser(@PathVariable("userId") UUID userId);
}
