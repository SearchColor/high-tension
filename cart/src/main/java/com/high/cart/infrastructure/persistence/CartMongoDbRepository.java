package com.high.cart.infrastructure.persistence;

import com.high.cart.domain.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CartMongoDbRepository extends MongoRepository<Cart, String> {
    Optional<Cart> findByUserId(String userId);
}
