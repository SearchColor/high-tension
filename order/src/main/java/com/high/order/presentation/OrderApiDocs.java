package com.high.order.presentation;

import com.high.order.application.dto.request.OrderItemDeliveryStatusChangeRequest;
import com.high.order.application.dto.request.OrderItemStatusChangeRequest;
import com.high.order.application.dto.request.OrderStatusChangeRequest;
import com.high.order.application.dto.request.OrderUpdateRequest;
import com.high.order.application.dto.response.OrderDetailResponse;
import com.high.order.application.dto.response.OrderItemCancelResponse;
import com.high.order.application.dto.response.OrderItemIdResponse;
import com.high.order.application.dto.response.OrderListResponse;
import com.high.order.application.dto.response.OrderResponse;
import com.library.jpa.response.PageResponse;
import com.library.module.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Order", description = "주문 API")
public interface OrderApiDocs {


    @Operation(summary = "주문 단건 조회 API", description = "주문 상세 내역을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = OrderDetailResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음", content = @Content)
    })
    ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
        @Parameter(description = "주문 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable("orderId") UUID orderId);




    @Operation(summary = "주문 전체 조회 API", description = "주문 전체 내역을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = OrderListResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
    })
    public ResponseEntity<ApiResponse<PageResponse<OrderListResponse>>> getOrders(
        @PageableDefault(size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable);




    @Operation(summary = "주문 취소 API", description = "특정 주문을 취소합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "취소 성공",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "취소할 수 없는 주문 상태", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음", content = @Content)
    })
    ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
        @Parameter(description = "주문 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID orderId);




    @Operation(summary = "주문 상품 부분 취소 API", description = "특정 주문의 특정 주문 상품을 취소합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "취소 성공",
            content = @Content(schema = @Schema(implementation = OrderItemIdResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "취소할 수 없는 상태", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문 또는 주문 상품을 찾을 수 없음", content = @Content)
    })
    ResponseEntity<ApiResponse<OrderItemCancelResponse>> cancelOrderItem(
        @Parameter(description = "주문 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID orderId,
        @Parameter(description = "주문 상품 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174001")
        @PathVariable UUID orderItemId);




    @Operation(summary = "주문 정보 변경 API", description = "주문의 배송지 정보를 변경합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 또는 변경할 수 없는 주문 상태", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음", content = @Content)
    })
    ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
        @Parameter(description = "주문 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID orderId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "주문 업데이트 요청 정보 (배송정보)",
            required = true,
            content = @Content(schema = @Schema(implementation = OrderUpdateRequest.class))
        )
        @Valid @RequestBody OrderUpdateRequest request);




    @Operation(summary = "주문 상태 변경 API", description = "주문의 상태(생성, 성공, 취소)를 변경합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 상태 변경 요청", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (MASTER 권한 필요)", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음", content = @Content)
    })
    ResponseEntity<ApiResponse<OrderResponse>> changeOrderStatus(
        @Parameter(description = "주문 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID orderId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "주문 상태 변경 요청 정보(CANCEL, SUCCESS)",
            required = true,
            content = @Content(schema = @Schema(implementation = OrderStatusChangeRequest.class))
        )
        @RequestBody OrderStatusChangeRequest request);




    @Operation(summary = "주문 환불 상태 변경 API", description = "특정 주문에 대한 환불을 신청하거나 처리합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공",
            content = @Content(schema = @Schema(implementation = OrderItemIdResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 환불 요청 또는 환불할 수 없는 상태", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문 또는 주문 상품을 찾을 수 없음", content = @Content)
    })
    ResponseEntity<ApiResponse<OrderItemIdResponse>> changeOrderItemStatus(
        @Parameter(description = "주문 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID orderId,
        @Parameter(description = "주문 상품 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174001")
        @PathVariable UUID orderItemId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "환불 상태 변경 요청 정보(RETURN_REQUEST, RETURNED)",
            required = true,
            content = @Content(schema = @Schema(implementation = OrderItemStatusChangeRequest.class))
        )
        @RequestBody @Valid OrderItemStatusChangeRequest request);




    @Operation(summary = "주문 배송 상태 변경 API", description = "특정 주문에 대한 배송 상태를 변경합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공",
            content = @Content(schema = @Schema(implementation = OrderItemIdResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 배송 상태 변경 요청", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (SELLER 또는 MASTER 권한 필요)", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문 또는 주문 상품을 찾을 수 없음", content = @Content)
    })
    ResponseEntity<ApiResponse<OrderItemIdResponse>> changeOrderItemDeliveryStatus(
        @Parameter(description = "주문 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID orderId,
        @Parameter(description = "주문 상품 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174001")
        @PathVariable UUID orderItemId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "배송 상태 변경 요청 정보",
            required = true,
            content = @Content(schema = @Schema(implementation = OrderItemDeliveryStatusChangeRequest.class))
        )
        @RequestBody @Valid OrderItemDeliveryStatusChangeRequest request);




    @Operation(summary = "주문 삭제 API", description = "주문을 삭제합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "삭제할 수 없는 주문 상태", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음", content = @Content)
    })
    ResponseEntity<ApiResponse<Void>> deleteOrder(
        @Parameter(description = "주문 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID orderId);




    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "처리 성공",
            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음", content = @Content)
    })
    ResponseEntity<ApiResponse<OrderResponse>> testApi(
        @Parameter(description = "주문 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID orderId);

}