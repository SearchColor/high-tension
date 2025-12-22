package com.high.payment.application.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.payment.application.adapter.UserServiceClient;
import com.high.payment.application.dto.CreatePaymentRequest;
import com.high.payment.application.dto.CreatePaymentResponse;
import com.high.payment.application.dto.IamportPaymentInfo;
import com.high.payment.application.dto.PaymentCompletedEvent;
import com.high.payment.application.dto.PaymentCreateCommandRequest;
import com.high.payment.application.dto.PaymentDetailResponse;
import com.high.payment.application.dto.PaymentSagaEventPort;
import com.high.payment.application.dto.PaymentSagaResultMessage;
import com.high.payment.application.port.out.IamportClientPort;
import com.high.payment.domain.model.Payment;
import com.high.payment.domain.model.PaymentOutbox;
import com.high.payment.domain.model.PaymentStatus;
import com.high.payment.domain.port.out.PaymentOutboxRepositoryPort;
import com.high.payment.domain.port.out.PaymentRepositoryPort;
import com.high.payment.domain.port.out.PaymentSagaRepositoryPort;
import com.high.payment.domain.model.PaymentSaga;
import com.high.payment.domain.model.PaymentSagaStatus;
import com.high.payment.exception.PaymentException;
import com.high.payment.exception.PaymentErrorCode;
import com.high.payment.infrastructure.kafka.dto.OrderDeleteRequestMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final PaymentOutboxRepositoryPort outboxRepository;
	private final IamportClientPort iamportClient;
	private final ObjectMapper objectMapper;
	private final PaymentRepositoryPort paymentRepositoryPort; // Hexagonal Port 사용
	private final UserServiceClient userServiceClient;
	private final PaymentSagaEventPort paymentSagaEventPort;
	private final PaymentSagaRepositoryPort paymentSagaRepositoryPort;

	@Override
	@Transactional // DB 저장과 Outbox 저장을 하나의 트랜잭션으로 묶음
	public void processPayment(CreatePaymentRequest event) {
		log.info("결제 프로세스 시작: OrderId={}", event.orderId());

		String pgTid = null;
		String eventType = "payment.completed";
		PaymentStatus initStatus = PaymentStatus.PENDING;

		Integer price = event.paymentPrice();
		if (price == null || price <= 0) {
			throw new PaymentException(PaymentErrorCode.PAYMENT_BAD_REQUEST, "paymentPrice는 1 이상이어야 합니다.");
		}

		// 1. 도메인 객체 생성 (아직 저장 안 함)
		Payment payment = Payment.builder()
								 .orderId(event.orderId())
								 .userId(event.userId())
								 .paymentPrice(price)
								 .paymentMethod(event.paymentMethod())
								 .status(initStatus)
								 .build();

		try {
			// 2. PG사 결제 승인 요청 (외부 연동)
			pgTid = iamportClient.requestPayment(event.orderId(), BigDecimal.valueOf(price), event.paymentMethod());

			// 3. 성공 시 상태 변경
			payment.complete(pgTid);
			log.info("PG 결제 승인 성공: TID={}", pgTid);

		} catch (Exception e) {
			// 4. 실패 시 상태 변경
			payment.fail();
			eventType = "payment.failed";
			log.error("PG 결제 승인 실패: {}", e.getMessage());
			throw new PaymentException(PaymentErrorCode.PG_CLIENT_ERROR, "PG 결제 승인 실패: " + e.getMessage());
		}

		// 5. Payment 저장 (Port 사용으로 통일)
		Payment savedPayment = paymentRepositoryPort.save(payment);

		// 6. Outbox 저장 (이벤트 발행 보장)
		try {
			PaymentCompletedEvent outboxPayload = new PaymentCompletedEvent(
				savedPayment.getId(),
				savedPayment.getOrderId(),
				savedPayment.getUserId(),
				savedPayment.getPaymentPrice(),
				savedPayment.getStatus().name()
			);

			String jsonPayload = objectMapper.writeValueAsString(outboxPayload);
			PaymentOutbox outbox = PaymentOutbox.create(savedPayment.getId(), eventType, jsonPayload);

			outboxRepository.save(outbox);

		} catch (JsonProcessingException e) {
			throw new PaymentException(PaymentErrorCode.PAYMENT_BAD_REQUEST, "Outbox JSON 변환 에러");
		}

	}

	@Override
	@Transactional
	public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
		// 1. DTO를 Entity로 변환 및 초기 상태 설정
		Payment payment = Payment.builder()
								 .orderId(request.orderId())
								 .userId(request.userId())
								 .paymentPrice(request.paymentPrice())
								 .status(PaymentStatus.PENDING) // PaymentStatus enum을 가정
								 .paymentMethod(request.paymentMethod())
								 .build();

		// 2. DB에 초기 결제 정보 저장
		Payment savedPayment = paymentRepositoryPort.save(payment);

		// 3. Outbox에 이벤트 저장 (나중에 Kafka로 발행됨)

		// 4. 응답 DTO 반환
		return CreatePaymentResponse.from(savedPayment);
	}

	@Override
	@Transactional
	public CreatePaymentResponse getPayment(UUID paymentId) {
		Payment payment = paymentRepositoryPort.findById(paymentId)
											   .orElseThrow(
												   () -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND,
																			  "결제 정보를 찾을 수 없습니다. ID: " + paymentId));
		return CreatePaymentResponse.from(payment);
	}

	@Override
	@Transactional(readOnly = true)
	public PaymentDetailResponse getPaymentByOrderId(UUID orderId) {
		Payment payment = paymentRepositoryPort.findByOrderId(orderId)
											   .orElseThrow(
												   () -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND,
																			  "OrderId " + orderId
																				  + "에 해당하는 결제 정보를 찾을 수 없습니다."));
		// DTO로 변환하여 반환
		return PaymentDetailResponse.from(payment);
	}

	@Override
	@Transactional
	public void verifyAndFinalizePayment(String impUid, String merchantUid) {

		// 1) merchantUid 유효성 + UUID 변환
		if (!StringUtils.hasText(merchantUid)) {
			log.error("MerchantUid가 누락되었습니다. Webhook 데이터 오류.");
			throw new PaymentException(PaymentErrorCode.PAYMENT_BAD_REQUEST, "MerchantUid가 누락되었습니다.");
		}

		final UUID orderId;
		try {
			orderId = UUID.fromString(merchantUid);
		} catch (IllegalArgumentException e) {
			log.error("MerchantUid 형식이 유효한 UUID가 아닙니다: {}", merchantUid);
			throw new PaymentException(PaymentErrorCode.PAYMENT_BAD_REQUEST, "유효하지 않은 MerchantUid 형식입니다.");
		}

		log.info("[Service] 결제 검증 시작: impUid={}, orderId={}", impUid, orderId);

		// 2) DB 결제 정보 조회
		Payment payment = paymentRepositoryPort.findByOrderId(orderId)
											   .orElseThrow(() -> new PaymentException(
												   PaymentErrorCode.PAYMENT_NOT_FOUND,
												   "DB에 결제 요청 정보가 없습니다. OrderId: " + orderId
											   ));

		// 3) PG 조회 (merchantUid 기반)
		final IamportPaymentInfo pgInfo;
		try {
			pgInfo = iamportClient.getPaymentInfoByMerchantUid(merchantUid);
		} catch (Exception e) {
			log.error("[Service] PG 결제 조회 실패. merchantUid={}", merchantUid, e);
			payment.fail();
			paymentRepositoryPort.save(payment);
			throw new PaymentException(PaymentErrorCode.PG_CLIENT_ERROR, "PG사 결제 조회 실패: " + e.getMessage());
		}

		// 4) 결제 상태 검증
		if (pgInfo == null || !StringUtils.hasText(pgInfo.getStatus())) {
			payment.fail();
			paymentRepositoryPort.save(payment);
			throw new PaymentException(PaymentErrorCode.PAYMENT_VERIFICATION_FAILED, "PG 결제 상태 조회 실패");
		}

		if (!"paid".equalsIgnoreCase(pgInfo.getStatus())) {
			log.error("[Service] PG 결제 상태가 paid가 아님. status={}, impUid={}", pgInfo.getStatus(), impUid);
			payment.fail();
			paymentRepositoryPort.save(payment);
			throw new PaymentException(PaymentErrorCode.PAYMENT_VERIFICATION_FAILED, "PG 결제 상태가 paid가 아닙니다.");
		}

		// 5) 금액 검증
		Integer expectedPrice = payment.getPaymentPrice();
		Integer pgPrice = pgInfo.getPaymentPrice();

		if (expectedPrice == null || pgPrice == null) {
			payment.fail();
			paymentRepositoryPort.save(payment);
			throw new PaymentException(PaymentErrorCode.PAYMENT_VERIFICATION_FAILED, "결제 금액 누락으로 인한 검증 실패.");
		}

		if (!expectedPrice.equals(pgPrice)) {
			// 금액 불일치면 취소 요청 후 실패 처리
			iamportClient.cancelPayment(impUid, BigDecimal.valueOf(pgPrice), "금액 불일치로 인한 취소");
			payment.fail();
			paymentRepositoryPort.save(payment);
			throw new PaymentException(PaymentErrorCode.PAYMENT_VERIFICATION_FAILED, "결제 금액 불일치로 인한 검증 실패.");
		}

		// 6) PG TID 검증 + 결제 확정
		String pgTid = pgInfo.getPgTid();
		if (!StringUtils.hasText(pgTid)) {
			log.error("[Service] PG TID 누락. impUid={}", impUid);
			payment.fail();
			paymentRepositoryPort.save(payment);
			throw new PaymentException(PaymentErrorCode.PG_CLIENT_ERROR, "PG사 거래번호(PG TID) 누락");
		}

		payment.complete(pgTid);
		paymentRepositoryPort.save(payment);

		// 7) Outbox 저장 (payment.completed)
		try {
			PaymentCompletedEvent outboxPayload = new PaymentCompletedEvent(
				payment.getId(),
				payment.getOrderId(),
				payment.getUserId(),
				payment.getPaymentPrice(),
				payment.getStatus().name()
			);

			String jsonPayload = objectMapper.writeValueAsString(outboxPayload);
			PaymentOutbox outbox = PaymentOutbox.create(payment.getId(), "payment.completed", jsonPayload);
			outboxRepository.save(outbox);

			log.info("[Service] 결제 완료 확정 + Outbox 저장 완료. orderId={}", orderId);

		} catch (JsonProcessingException e) {
			throw new PaymentException(PaymentErrorCode.PAYMENT_BAD_REQUEST, "Outbox JSON 변환 에러");
		}
	}

	@Override
	@Transactional
	public void cancelPayment(UUID orderId) {

		log.info(" [Service] 결제 취소 시작: OrderId={}", orderId);

		// 1. 결제 조회
		Payment payment = paymentRepositoryPort.findByOrderId(orderId)
											   .orElseThrow(
												   () -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND,
																			  "결제 정보를 찾을 수 없습니다."));

		// 2. 이미 취소되었거나 완료되지 않은 건인지 확인
		if (payment.getStatus() != PaymentStatus.COMPLETED) {
			throw new PaymentException(PaymentErrorCode.PAYMENT_CANCELLATION_NOT_ALLOWED, "취소할 수 없는 상태입니다.");
		}

		if (!StringUtils.hasText(payment.getPgTid())) {
			log.error("PG TID 누락, 환불에 필요한 PG TID가 없습니다. OrderId={}", orderId);
			throw new PaymentException(PaymentErrorCode.PAYMENT_BAD_REQUEST, "PG사에 환불 요청할 거래 번호(PG TID)가 누락되었습니다.");
		}

		// 3. PG사 환불 요청 (Mock)
		try {
			iamportClient.cancelPayment(
				payment.getPgTid(),
				BigDecimal.valueOf(payment.getPaymentPrice()),
				"고객 요청 취소"
			);
			log.info("[Service] PG사 취소 요청 성공");
		} catch (Exception e) {
			throw new PaymentException(PaymentErrorCode.PG_CLIENT_ERROR, "PG사 환불 실패: " + e.getMessage());
		}

		// 4. DB 상태 변경 (CANCELED)
		payment.cancel();
		paymentRepositoryPort.save(payment);

		// 5. Outbox 이벤트 저장 (payment.canceled)
		try {
			// 1) payment.canceled (결제 도메인 이벤트)
			String paymentCanceledPayload = objectMapper.writeValueAsString(payment);

			PaymentOutbox paymentCanceledOutbox = PaymentOutbox.create(
				payment.getId(),
				"payment.canceled",
				paymentCanceledPayload
			);
			outboxRepository.save(paymentCanceledOutbox);

			// 2) order-delete-request (주문 취소 요청 이벤트)
			UUID sagaId = UUID.randomUUID();

			OrderDeleteRequestMessage orderDeleteMsg = new OrderDeleteRequestMessage(
				sagaId,
				payment.getOrderId(),
				payment.getUserId()
			);

			String orderDeletePayload = objectMapper.writeValueAsString(orderDeleteMsg);

			PaymentOutbox orderDeleteOutbox = PaymentOutbox.create(
				payment.getId(),
				"order.delete.requested",
				orderDeletePayload,
				"order-delete-request",
				payment.getOrderId().toString()
			);

			outboxRepository.save(orderDeleteOutbox);

			log.info("[Outbox] 결제취소 이벤트 저장 완료. orderId={}, sagaId={}", payment.getOrderId(), sagaId);

		} catch (JsonProcessingException e) {
			throw new PaymentException(PaymentErrorCode.PAYMENT_BAD_REQUEST, "JSON 변환 실패");
		}
	}

	@Override
	@Transactional
	public void processPaymentSaga(PaymentCreateCommandRequest command) {

		// DTO가 Record이므로, command.필드명()이 올바른 접근 방식입니다.
		UUID sagaId = command.sagaId();
		UUID orderId = command.orderId();
		UUID userId = command.userId();
		Integer paymentPrice = command.paymentPrice();

		log.info("[Saga] 결제 Intent 생성 요청 수신: SagaId={}, OrderId={}, UserId={}, Price={}",
				 sagaId, orderId, userId, paymentPrice);

		// 0) paymentPrice 검증 (Intent 생성 단계에서도 최소 검증은 필요)
		if (paymentPrice == null || paymentPrice <= 0) {
			PaymentSagaResultMessage fail = new PaymentSagaResultMessage(
				sagaId, orderId, userId, false,
				"paymentPrice는 1 이상이어야 합니다.",
				PaymentErrorCode.PAYMENT_BAD_REQUEST.getCode()
			);
			paymentSagaEventPort.publishPaymentResult(fail);
			return;
		}

		// 1) 멱등 처리: sagaId로 먼저 조회해서 이미 SUCCESS/FAIL이면 즉시 종료
		var existingSagaOpt = paymentSagaRepositoryPort.findBySagaId(sagaId);
		if (existingSagaOpt.isPresent()) {
			var existingSaga = existingSagaOpt.get();

			if (existingSaga.getStatus() != PaymentSagaStatus.PENDING) {
				log.warn("[Saga] 이미 처리된 SagaId={} status={} - skip", sagaId, existingSaga.getStatus());
				return;
			}
		}

		// 2) saga row 없으면 생성(PENDING)
		var saga = existingSagaOpt.orElseGet(() ->
												 paymentSagaRepositoryPort.save(PaymentSaga.start(sagaId, orderId, userId)
												 ));

		try {
			// 3) 유저 검증 (필요하면 유지)
			userServiceClient.validateUser(userId);

			// 4) 결제 레코드(PENDING) 생성만 한다. (PG 승인 X, complete() X)
			Payment payment = Payment.builder()
									 .orderId(orderId)
									 .userId(userId)
									 .paymentPrice(paymentPrice)
									 .paymentMethod("CARD")
									 .status(PaymentStatus.PENDING)
									 .build();

			Payment saved = paymentRepositoryPort.save(payment);

			// 5) 오케스트레이터에게 “결제 생성 성공” 알림 -> true: 결제 완료가 아닌 결제 생성
			saga.success("Payment saga success");
			paymentSagaRepositoryPort.save(saga);

			// 6) 오케스트레이터 결과 발행
			PaymentSagaResultMessage successResult = new PaymentSagaResultMessage(
				sagaId, orderId, userId, true, "Payment successful", null
			);
			paymentSagaEventPort.publishPaymentResult(successResult);

			log.info("[Saga] 결제 Intent 생성 완료: paymentId={}", saved.getId());

		} catch (Exception e) {
			log.error("[Saga] 결제 Intent 생성 실패: {}", e.getMessage(), e);

			PaymentSagaResultMessage fail = new PaymentSagaResultMessage(
				sagaId,
				orderId,
				userId,
				false,
				"Payment intent create failed: " + e.getMessage(),
				5003
			);
			paymentSagaEventPort.publishPaymentResult(fail);
		}
	}
}