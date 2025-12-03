package com.high.cart.presentation.controller;

import com.high.cart.application.dto.request.CartItemRequestDto;
import com.high.cart.application.dto.request.CreateCartRequestDto;
import com.high.cart.application.dto.response.CartResponseDto;
import com.high.cart.application.dto.response.CreateCartResponseDto;
import com.high.cart.application.service.CartServiceV1;
import com.high.cart.domain.model.CartItem;
import com.library.module.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartControllerV1 {

    private final CartServiceV1 serviceV1;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateCartResponseDto>> createCart(@RequestBody CreateCartRequestDto requestDto){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(serviceV1.saveCart(requestDto),"장바구니 생성"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<CartResponseDto>> getCart(@PathVariable String userId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(serviceV1.getCartByUserId(userId),"장바구니 조회"));
    }

    @PutMapping("/user/{userId}/addProduct")
    public ResponseEntity<ApiResponse<CartResponseDto>> addProductToCart(
            @PathVariable String userId,
            @RequestBody CartItemRequestDto requestDto){
        CartItem cartItem = CartItemRequestDto.createCartItem(
                requestDto.getProductId(),
                requestDto.getQuantity(),
                requestDto.getPrice()
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(serviceV1.addItemToCart(userId, cartItem),"장바구니 상품 추가"));
    }

    @PutMapping("/user/{userId}/deleteProduct")
    public ResponseEntity<ApiResponse<CartResponseDto>> deleteProductToCart(
            @PathVariable String userId,
            @RequestParam String productId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(serviceV1.deleteItemToCart(userId, productId),"장바구니 상품 삭제"));
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<CartResponseDto>> deleteAllToCart(@PathVariable String userId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(serviceV1.deleteAllToCart(userId),"장바구니 상품 전체 삭제"));
    }
}
