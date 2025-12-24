package com.high.payment.application.adapter;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.high.payment.domain.port.out.UserValidationPort;

@FeignClient(name ="user-service", url = "${user-service.url}")
public interface UserServiceClient extends UserValidationPort {
	// 유저 유효성 검증 API 호출을 위한 메서드 정의
	@Override
	@GetMapping("/users/{userId}/validate")
	void validateUser(@PathVariable("userId") UUID userId);
}
