package com.high.product.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.high.product.domain.model.Limited_Product_Stock;

public interface JpaLimited_StockRepository extends JpaRepository<Limited_Product_Stock, UUID> {


	boolean existsByLimitedProductId(UUID limitedProductId);
}
