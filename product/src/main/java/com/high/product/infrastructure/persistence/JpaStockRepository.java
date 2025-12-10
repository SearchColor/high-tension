package com.high.product.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.high.product.domain.model.Product_Stock;

import jakarta.persistence.LockModeType;

public interface JpaStockRepository extends JpaRepository<Product_Stock, UUID> {

	// 상품 ID로 재고 조회
	Optional<Product_Stock> findByProductId(UUID productId);

	// 상품 ID로 재고 존재 여부 확인
	boolean existsByProductId(UUID productId);

	// 비관적 락 적용
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from Product_Stock p where p.productId = :productId")
	Optional<Product_Stock> findByProductIdForUpdate(UUID productId);
}
