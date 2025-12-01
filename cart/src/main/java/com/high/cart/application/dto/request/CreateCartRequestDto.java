package com.high.cart.application.dto.request;


import com.high.cart.domain.model.CartItem;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CreateCartRequestDto {

    private String userId;
    private List<CartItem> items;
}
