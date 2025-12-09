package com.high.payment.presentation;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.high.payment.application.dto.CreatePaymentRequest;
import com.high.payment.application.dto.CreatePaymentResponse;
import com.high.payment.application.dto.IamportWebhookDto;
import com.high.payment.application.dto.PaymentDetailResponse;
import com.high.payment.application.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {
	private final PaymentService paymentService;

	/**
	 * [Webhook Endpoint] 아임포트(Iamport)로부터 결제 상태 변경 알림을 수신
	 * * @param webhookDto 아임포트가 전송한 Webhook 데이터
	 */
	@PostMapping("/iamport/webhook")
	public ResponseEntity<Void> handleIamportWebhook(@RequestBody IamportWebhookDto webhookDto) {

		log.info("Iamport Webhook 수신: MerchantUid={}, Status={}", webhookDto.merchantUid(), webhookDto.status());

		if ("paid".equals(webhookDto.status())) {

			try {
				paymentService.verifyAndFinalizePayment(webhookDto.impUid(), webhookDto.merchantUid());
				log.info("Webhook 처리 성공. Payment 확정 완료.");
			} catch (Exception e) {
				log.error(" Webhook 처리 중 오류 발생 (정합성 검증 실패 등): {}", e.getMessage(), e);
				return ResponseEntity.status(HttpStatus.OK).build();
			}

		} else if ("failed".equals(webhookDto.status()) || "cancelled".equals(webhookDto.status())) {
			log.warn("결제 실패 또는 취소 Webhook 수신. MerchantUid={}", webhookDto.merchantUid());
		}

		return ResponseEntity.status(HttpStatus.OK).build();
	}

	// 결제 생성 API (POST /api/v1/payments)
	@PostMapping
	public ResponseEntity<CreatePaymentResponse> createPayment(@RequestBody CreatePaymentRequest request) {
		log.info("결제 생성 요청 수신: OrderId={}, Amount={}", request.orderId(), request.amount());

		// 1. 서비스 인터페이스를 통해 비즈니스 로직 호출
		CreatePaymentResponse response = paymentService.createPayment(request);

		// 2. 성공 시 201 Created 응답 반환
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// 결제 조회 API (GET /api/v1/payments/{paymentId})
	@GetMapping("/{paymentId}")
	public ResponseEntity<CreatePaymentResponse> getPayment(
		@PathVariable UUID paymentId) {

		// 1. PathVariable 데이터 로그 추가
		log.info("[Payment] 결제 조회 요청 수신: PaymentId={}", paymentId);

		CreatePaymentResponse response = paymentService.getPayment(paymentId);

		// 2. 서비스 처리 후 응답 데이터 상세 로그 추가
		log.info("[Payment] 결제 조회 성공. PaymentId={}, OrderId={}, Amount={}",
				 response.paymentId(), response.orderId(), response.amount());

		// 3. 200 OK 응답 반환
		return ResponseEntity.ok(response);
	}

	@GetMapping("/order/{orderId}")
	public ResponseEntity<PaymentDetailResponse> getPaymentByOrderId(@PathVariable UUID orderId) {

		PaymentDetailResponse response = paymentService.getPaymentByOrderId(orderId);

		return ResponseEntity.ok(response);
	}

	// 결제 취소 요청
	@PostMapping("/{orderId}/cancel")
	public ResponseEntity<Void> cancelPayment(@PathVariable UUID orderId) {
		// 취소 로직 호출
		paymentService.cancelPayment(orderId);
		return ResponseEntity.noContent().build(); // 204 No Content
	}
}
