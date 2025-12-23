package com.high.cart.presentation.controller;

import com.high.cart.application.dto.request.CartItemRequestDto;
import com.high.cart.application.dto.request.CreateCartRequestDto;
import com.high.cart.application.dto.response.CartResponseDto;
import com.high.cart.application.dto.response.CreateCartResponseDto;
import com.library.module.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Cart API", description = "장바구니 관리 API")
public interface CartControllerSwagger {

    @Operation(
            summary = "장바구니 생성",
            description = "새로운 사용자의 장바구니를 생성합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "장바구니 생성 성공",
                    content = @Content(schema = @Schema(implementation = CreateCartResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content
            )
    })
    ResponseEntity<ApiResponse<CreateCartResponseDto>> createCart(
            @Parameter(description = "장바구니 생성 요청 정보", required = true)
            @RequestBody CreateCartRequestDto requestDto
    );

    @Operation(
            summary = "장바구니 조회",
            description = "사용자 ID로 장바구니 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "장바구니 조회 성공",
                    content = @Content(schema = @Schema(implementation = CartResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "장바구니를 찾을 수 없음",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content
            )
    })
    ResponseEntity<ApiResponse<CartResponseDto>> getCart(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable String userId
    );

    @Operation(
            summary = "장바구니에 상품 추가",
            description = "사용자의 장바구니에 상품을 추가합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 추가 성공",
                    content = @Content(schema = @Schema(implementation = CartResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "장바구니 또는 상품을 찾을 수 없음",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content
            )
    })
    ResponseEntity<ApiResponse<CartResponseDto>> addProductToCart(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable String userId,
            @Parameter(description = "장바구니 상품 추가 정보", required = true)
            @RequestBody CartItemRequestDto requestDto
    );

    @Operation(
            summary = "장바구니 상품 삭제",
            description = "사용자의 장바구니에서 특정 상품을 삭제합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 삭제 성공",
                    content = @Content(schema = @Schema(implementation = CartResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "장바구니 또는 상품을 찾을 수 없음",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content
            )
    })
    ResponseEntity<ApiResponse<CartResponseDto>> deleteProductToCart(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable String userId,
            @Parameter(description = "삭제할 상품 ID", required = true)
            @RequestParam String productId
    );

    @Operation(
            summary = "장바구니 전체 상품 삭제",
            description = "사용자의 장바구니에 있는 모든 상품을 삭제합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "전체 상품 삭제 성공",
                    content = @Content(schema = @Schema(implementation = CartResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "장바구니를 찾을 수 없음",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content
            )
    })
    ResponseEntity<ApiResponse<CartResponseDto>> deleteAllToCart(
            @Parameter(description = "사용자 ID", required = true, example = "user123")
            @PathVariable String userId
    );
}