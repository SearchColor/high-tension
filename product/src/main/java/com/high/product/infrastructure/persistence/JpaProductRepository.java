package com.high.product.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.high.product.domain.model.Product;

public interface JpaProductRepository extends JpaRepository<Product, UUID> {

	// soft delete 제외하고 조회
	Optional<Product> findByIdAndDeletedAtIsNull(UUID id);

	// 카테고리별 조회 (soft delete 제외)
	Page<Product> findAllByCategoryAndDeletedAtIsNull(String category, Pageable pageable);


}
