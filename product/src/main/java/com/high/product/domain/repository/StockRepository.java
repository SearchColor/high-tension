package com.high.product.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.high.product.domain.model.Product_Stock;

import jakarta.persistence.LockModeType;

public interface StockRepository {

	Optional<Product_Stock> findById(UUID id);

	Product_Stock save(Product_Stock stock);


	Page<Product_Stock> findAll(Pageable pageable); // 메서드 추가

	Optional<Product_Stock> findByProductId(UUID productId);

	boolean existsByProductId(UUID productId);

	// 비관적 락 적용
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from Product_Stock p where p.productId = :productId")
	Optional<Product_Stock> findByProductIdForUpdate(UUID productId);
}
