package com.high.payment.infrastrucutre.adapter.out.client;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.high.payment.application.port.out.IamportClientPort;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IamportClientAdapter implements IamportClientPort {

	// TODO: 실제 아임포트 API 연동 로직이 들어갈 곳 (현재는 Mock 처리)
	@Override
	public String requestPayment(UUID orderId, BigDecimal amount, String method) {
		log.info("[Iamport] 결제 요청 전송: OrderId={}, Amount={}", orderId, amount);

		// 시뮬레이션: 짝수 초에는 성공, 홀수 초에는 실패 등으로 테스트 가능
		// 여기서는 무조건 성공하는 더미 ID 반환
		return "imp_" + UUID.randomUUID().toString().substring(0, 10);

		// 실패 테스트 시 아래 주석 해제
		// throw new RuntimeException("잔액 부족");
	}
}
