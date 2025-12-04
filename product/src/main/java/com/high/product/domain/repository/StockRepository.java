package com.high.product.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.high.product.domain.model.Product_Stock;

public interface StockRepository extends JpaRepository<Product_Stock, UUID> {

	Optional<Product_Stock> findByProductId(UUID productId);

	boolean existsByProductId(UUID productId);
}
