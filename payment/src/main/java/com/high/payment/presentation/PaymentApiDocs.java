package com.high.payment.presentation;


import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.high.payment.application.dto.CreatePaymentRequest;
import com.high.payment.application.dto.CreatePaymentResponse;
import com.high.payment.application.dto.IamportWebhookDto;
import com.high.payment.application.dto.PaymentDetailResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Payment", description = "결제 API")
public interface PaymentApiDocs {

	@Operation(
		summary = "아임포트 Webhook 수신",
		description = "아임포트 결제 상태 변경 Webhook을 수신합니다. status=paid인 경우 결제 검증 및 확정(완료 처리)을 수행합니다.",
		security = {} // 문서상 인증 제외
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Webhook 수신 성공", content = @Content)
	})
	ResponseEntity<Void> handleIamportWebhook(
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "아임포트 Webhook Payload",
			required = true,
			content = @Content(schema = @Schema(implementation = IamportWebhookDto.class))
		)
		@RequestBody IamportWebhookDto webhookDto
	);


	@Operation(
		summary = "결제 생성",
		description = "주문 기준으로 결제를 생성합니다. (결제 요청 레코드 생성 + PG 요청/Mock 처리)",
		security = { @SecurityRequirement(name = "bearerAuth") }
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "201", description = "결제 생성 성공",
			content = @Content(schema = @Schema(implementation = CreatePaymentResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 오류", content = @Content)
	})
	ResponseEntity<CreatePaymentResponse> createPayment(
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "결제 생성 요청",
			required = true,
			content = @Content(schema = @Schema(implementation = CreatePaymentRequest.class))
		)
		@RequestBody CreatePaymentRequest request
	);


	@Operation(
		summary = "결제 단건 조회(paymentId)",
		description = "paymentId로 결제 정보를 조회합니다.",
		security = { @SecurityRequirement(name = "bearerAuth") }
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200", description = "조회 성공",
			content = @Content(schema = @Schema(implementation = CreatePaymentResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "결제 정보 없음", content = @Content)
	})
	ResponseEntity<CreatePaymentResponse> getPayment(
		@Parameter(description = "결제 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
		@PathVariable UUID paymentId
	);


	@Operation(
		summary = "주문 기준 결제 조회(orderId)",
		description = "orderId로 결제 정보를 조회합니다.",
		security = { @SecurityRequirement(name = "bearerAuth") }
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200", description = "조회 성공",
			content = @Content(schema = @Schema(implementation = PaymentDetailResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "결제 정보 없음", content = @Content)
	})
	ResponseEntity<PaymentDetailResponse> getPaymentByOrderId(
		@Parameter(description = "주문 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
		@PathVariable UUID orderId
	);


	@Operation(
		summary = "결제 취소 요청",
		description = "orderId 기준으로 결제 취소(환불)를 요청합니다.",
		security = { @SecurityRequirement(name = "bearerAuth") }
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "취소 성공", content = @Content),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "결제 정보 없음", content = @Content),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "취소 불가능 상태", content = @Content)
	})
	ResponseEntity<Void> cancelPayment(
		@Parameter(description = "주문 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
		@PathVariable UUID orderId
	);
}
