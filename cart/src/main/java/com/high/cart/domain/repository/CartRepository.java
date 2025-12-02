package com.high.cart.domain.repository;

import com.high.cart.domain.model.Cart;

import java.util.Optional;

public interface CartRepository {

    Cart save(Cart cart);
    Optional<Cart> findByUserId(String userId);
    boolean existsByUserId(String userId);  // 추가
}
