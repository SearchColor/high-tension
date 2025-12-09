package com.high.payment.infrastrcutre.adapter.out.client;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.high.payment.application.dto.IamportPaymentInfo;
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

	@Override
	public IamportPaymentInfo getPaymentInfo(String impUid) {
		log.info(" [PG Mock] PG사 API 호출 시뮬레이션: impUid={}", impUid);

		if (impUid.contains("success")) {
			// 성공 응답 (25000.00원, status: paid)
			return IamportPaymentInfo.builder()
									 .impUid(impUid)
									 .merchantUid("b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d6e")
									 .amount(BigDecimal.valueOf(25000))
									 .status("paid")
									 .pgTid("TID-MOCK-" + impUid.toUpperCase())
									 .build();
		} else {
			// 실패 응답 (status: failed)
			return IamportPaymentInfo.builder()
									 .impUid(impUid)
									 .merchantUid("b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d6e")
									 .amount(BigDecimal.valueOf(0))
									 .status("failed")
									 .pgTid(null)
									 .build();
		}
	}

	@Override
	public void cancelPayment(String impUid, BigDecimal amount, String reason) {
		log.warn(" [PG Mock] PG사에 결제 취소 요청 시뮬레이션 실행: ImpUid={}, Amount={}, Reason={}",
				 impUid, amount, reason);
		// 실제로는 여기에서 외부 API 호출 및 응답 처리가 이루어집니다.
	}
}
