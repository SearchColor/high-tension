package com.high.payment.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.payment.application.dto.CreatePaymentRequest;
import com.high.payment.application.dto.CreatePaymentResponse;
import com.high.payment.application.dto.IamportPaymentInfo;
import com.high.payment.application.dto.PaymentCompletedEvent;
import com.high.payment.application.dto.PaymentDetailResponse;
import com.high.payment.application.port.out.IamportClientPort;
import com.high.payment.domain.model.Payment;
import com.high.payment.domain.model.PaymentOutbox;
import com.high.payment.domain.model.PaymentStatus;
import com.high.payment.domain.port.out.PaymentOutboxRepositoryPort;
import com.high.payment.domain.port.out.PaymentRepositoryPort;

import com.high.payment.domain.repository.PaymentRepository; // 사용되지 않을 수 있지만, 임시로 유지

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	// private final PaymentRepository paymentRepository; //
	private final PaymentOutboxRepositoryPort outboxRepository;
	private final IamportClientPort iamportClient;
	private final ObjectMapper objectMapper;
	private final PaymentRepositoryPort paymentRepositoryPort; // Hexagonal Port 사용

	@Override
	@Transactional // DB 저장과 Outbox 저장을 하나의 트랜잭션으로 묶음
	public void processPayment(CreatePaymentRequest event) {
		log.info("결제 프로세스 시작: OrderId={}", event.orderId());

		String pgTid = null;
		String eventType = "payment.completed";
		PaymentStatus initStatus = PaymentStatus.REQUESTED;

		// 1. 도메인 객체 생성 (아직 저장 안 함)
		Payment payment = Payment.builder()
								 .orderId(event.orderId())
								 .userId(event.userId())
								 .amount(event.amount())
								 .paymentMethod(event.paymentMethod())
								 .status(initStatus)
								 .build();

		try {
			// 2. PG사 결제 승인 요청 (외부 연동)
			pgTid = iamportClient.requestPayment(event.orderId(), event.amount(), event.paymentMethod());

			// 3. 성공 시 상태 변경
			payment.complete(pgTid);
			log.info("PG 결제 승인 성공: TID={}", pgTid);

		} catch (Exception e) {
			// 4. 실패 시 상태 변경
			payment.fail();
			eventType = "payment.failed";
			log.error("PG 결제 승인 실패: {}", e.getMessage());
		}

		// 5. Payment 저장 (Port 사용으로 통일)
		Payment savedPayment = paymentRepositoryPort.save(payment);

		// 6. Outbox 저장 (이벤트 발행 보장)
		try {
			PaymentCompletedEvent outboxPayload = new PaymentCompletedEvent(
				savedPayment.getId(),
				savedPayment.getOrderId(),
				savedPayment.getUserId(),
				savedPayment.getAmount(),
				savedPayment.getStatus().name()
			);

			String jsonPayload = objectMapper.writeValueAsString(outboxPayload);
			PaymentOutbox outbox = PaymentOutbox.create(savedPayment.getId(), eventType, jsonPayload);

			outboxRepository.save(outbox);

		} catch (JsonProcessingException e) {
			throw new RuntimeException("Outbox JSON 변환 에러", e);
		}

	}

	@Override
	@Transactional
	public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
		// 1. DTO를 Entity로 변환 및 초기 상태 설정
		Payment payment = Payment.builder()
								 .orderId(request.orderId())
								 .userId(request.userId())
								 .amount(request.amount())
								 .status(PaymentStatus.REQUESTED) // PaymentStatus enum을 가정
								 .paymentMethod(request.paymentMethod())
								 .build();

		// 2. DB에 초기 결제 정보 저장
		Payment savedPayment = paymentRepositoryPort.save(payment);

		// 3. Outbox에 이벤트 저장 (나중에 Kafka로 발행됨)
		// paymentOutboxRepositoryPort.save(createPaymentRequestedEvent(savedPayment)); // 주석 처리 유지

		// 4. 응답 DTO 반환
		return CreatePaymentResponse.from(savedPayment);
	}

	@Override
	@Transactional
	public CreatePaymentResponse getPayment(UUID paymentId) {
		Payment payment = paymentRepositoryPort.findById(paymentId)
											   .orElseThrow(
												   () -> new RuntimeException("결제 정보를 찾을 수 없습니다. ID: " + paymentId));

		return CreatePaymentResponse.from(payment);
	}

	@Override
	@Transactional(readOnly = true)
	public PaymentDetailResponse getPaymentByOrderId(UUID orderId) {
		Payment payment = paymentRepositoryPort.findByOrderId(orderId)
										   .orElseThrow(() -> new RuntimeException("OrderId " + orderId + "에 해당하는 결제 정보를 찾을 수 없습니다."));

		// DTO로 변환하여 반환
		return PaymentDetailResponse.from(payment);
	}

	@Override
	@Transactional
	public void verifyAndFinalizePayment(String impUid, String merchantUid) {

		//  1. MerchantUid 필수 값 체크 및 UUID 변환 (오류 발생 방지 로직)
		if (!StringUtils.hasText(merchantUid)) { // StringUtils.hasText(String)는 null 또는 공백을 체크합니다.
			log.error("MerchantUid가 누락되었습니다. Webhook 데이터 오류.");
			throw new IllegalArgumentException("MerchantUid가 누락되었습니다.");
		}

		UUID orderId;
		try {
			// MerchantUid를 UUID로 변환 시도
			orderId = UUID.fromString(merchantUid);
		} catch (IllegalArgumentException e) {
			log.error("MerchantUid 형식이 유효한 UUID가 아닙니다: {}", merchantUid);
			throw new IllegalArgumentException("유효하지 않은 MerchantUid 형식입니다.", e);
		}

		log.info(" [Service] 결제 검증 시작: ImpUid={}, OrderId={}", impUid, orderId);

		// 2. DB에서 초기 결제 정보 조회 (Order ID 사용)
		Payment payment = paymentRepositoryPort.findByOrderId(orderId)
											   .orElseThrow(() -> new RuntimeException("DB에 결제 요청 정보가 없습니다. OrderId: " + orderId));

		// 3. PG사에서 실제 결제 정보 조회 (IamportClientPort 사용)
		IamportPaymentInfo pgInfo = iamportClient.getPaymentInfo(impUid);

		String pgTid = pgInfo.getPgTid();
		if (!StringUtils.hasText(pgTid)) {
			log.error(" PG사로부터 유효한 PG TID를 받지 못했습니다. ImpUid={}", impUid);
			payment.fail();
			paymentRepositoryPort.save(payment);
			throw new RuntimeException("PG사 정보 조회 실패: 유효한 거래번호(PG TID)가 누락되었습니다.");
		}

		// 4. 결제 검증 (금액 일치 여부 확인)
		if (payment.getAmount().compareTo(pgInfo.getAmount()) != 0) {
			log.error("금액 불일치 탐지! DB: {} vs PG: {}. 위변조 방지 취소 요청.",
					  payment.getAmount(), pgInfo.getAmount());

			// 4-1. PG사에 결제 취소 요청
			iamportClient.cancelPayment(impUid, pgInfo.getAmount(), "금액 불일치로 인한 취소");

			// 4-2. DB 상태 FAIL로 변경
			payment.fail();
			paymentRepositoryPort.save(payment);

			throw new RuntimeException("결제 금액 불일치로 인한 검증 실패.");
		}

		// 5. 결제 완료 상태 확정 및 DB 저장
		payment.complete(pgTid);
		paymentRepositoryPort.save(payment);

		// 6. Outbox 저장 및 이벤트 발행 (주문 서비스 통보)
		try {
			PaymentCompletedEvent outboxPayload = new PaymentCompletedEvent(
				payment.getId(), payment.getOrderId(), payment.getUserId(),
				payment.getAmount(), payment.getStatus().name()
			);

			String jsonPayload = objectMapper.writeValueAsString(outboxPayload);
			PaymentOutbox outbox = PaymentOutbox.create(payment.getId(), "payment.completed", jsonPayload);
			outboxRepository.save(outbox);
			log.info("Outbox 저장 완료. 결제 완료 이벤트 발행 예정.");

		} catch (JsonProcessingException e) {
			throw new RuntimeException("Outbox JSON 변환 에러", e);
		}
	}

	@Override
	@Transactional
	public void cancelPayment(UUID orderId) {

		log.info(" [Service] 결제 취소 시작: OrderId={}", orderId);

		// 1. 결제 조회
		Payment payment = paymentRepositoryPort.findByOrderId(orderId)
											   .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다."));

		// 2. 이미 취소되었거나 완료되지 않은 건인지 확인
		if (payment.getStatus() != PaymentStatus.COMPLETED) {
			throw new RuntimeException("취소할 수 없는 상태입니다.");
		}

		if (!StringUtils.hasText(payment.getPgTid())) {
			log.error("PG TID 누락, 환불에 필요한 PG TID가 없습니다. OrderId={}", orderId);
			throw new RuntimeException("PG사에 환불 요청할 거래 번호(PG TID)가 누락되었습니다.");
		}

		// 3. PG사 환불 요청 (Mock)
		try {
			iamportClient.cancelPayment(payment.getPgTid(), payment.getAmount(), "고객 요청 취소");
			log.info("[Service] PG사 취소 요청 성공");
		} catch (Exception e) {
			throw new RuntimeException("PG사 환불 실패: " + e.getMessage());
		}

		// 4. DB 상태 변경 (CANCELED)
		payment.cancel();
		paymentRepositoryPort.save(payment);

		// 5. Outbox 이벤트 저장 (payment.canceled)
		try {
			// 이벤트 페이로드 생성
			// Payment 객체 자체를 Payload로 사용하거나, 별도 DTO 생성
			String payload = objectMapper.writeValueAsString(payment);

			// Outbox 생성 (인자 3개: aggregateId, eventType, payload)
			PaymentOutbox outbox = PaymentOutbox.create(
				payment.getId(),
				"payment.canceled",
				payload
			);

			outboxRepository.save(outbox);
			log.info("[Outbox] 취소 이벤트 저장 완료.");

		} catch (JsonProcessingException e) {
			throw new RuntimeException("JSON 변환 실패", e);
		}
	}
}