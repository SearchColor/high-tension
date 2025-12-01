package com.high.cart.infrastructure.persistence;

import com.high.cart.domain.model.Cart;
import com.high.cart.domain.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@RequiredArgsConstructor
@Repository
public class CartMongoRepositoryImpl implements CartRepository {

    private final CartMongoDbRepository cartMongoRepository;

    @Override
    public Cart save(Cart cart) {
       return cartMongoRepository.save(cart);
    }

    @Override
    public Optional<Cart> findByUserId(String userId) {
        return cartMongoRepository.findByUserId(userId);
    }
}
