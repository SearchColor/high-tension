package com.high.cart.application.dto.request;

import com.high.cart.domain.model.CartItem;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartItemRequestDto {

    private String productId;
    private int quantity;
    private long price;


    public static CartItem createCartItem(String productId, int quantity, long price){
        return CartItem.builder()
                .productId(productId)
                .quantity(quantity)
                .price(price)
                .build();
    }
}
