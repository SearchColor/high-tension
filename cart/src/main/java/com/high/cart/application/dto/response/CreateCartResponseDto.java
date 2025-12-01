package com.high.cart.application.dto.response;

import com.high.cart.domain.model.Cart;
import com.high.cart.domain.model.CartItem;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CreateCartResponseDto {

    private final String cartId;
    private final String userId;
    private final List<CartItem> items;
    private long totalPrice ;

    public static CreateCartResponseDto from(
            Cart cart
    ){
        return CreateCartResponseDto.builder()
                .cartId(cart.getId())
                .userId(cart.getUserId())
                .items(cart.getItems())
                .totalPrice(cart.getTotalPrice())
                .build();
    }
}
