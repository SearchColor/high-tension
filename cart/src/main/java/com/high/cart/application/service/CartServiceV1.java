package com.high.cart.application.service;

import com.high.cart.domain.exception.CartNotFoundException;
import com.high.cart.domain.model.Cart;
import com.high.cart.domain.model.CartItem;
import com.high.cart.domain.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CartServiceV1 {

    private final CartRepository cartRepository;

    public Cart saveCart(Cart cart){
        cartRepository.save(cart);
        return cart;
    }

    public Cart getCartByUserId(String userId){
        return cartRepository.findByUserId(userId).orElseThrow(CartNotFoundException::new);
    }

    public Cart addItemToCart(String userId, CartItem item) {

        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> Cart.builder().userId(userId).build());
        cart.addItem(item);
        cartRepository.save(cart);
        return cart;
    }

}
