package com.high.cart.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartItem {
    private String productId;
    private int quantity;
    private long price;


    public void setQuantity(int quantity){
        this.quantity = quantity;
    }

    public void setPrice(long price){
        this.price = price;
    }
}
