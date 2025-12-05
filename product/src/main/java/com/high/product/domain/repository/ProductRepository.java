package com.high.product.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.high.product.domain.model.Product;

public interface ProductRepository {

	Optional<Product> findById(UUID id);

	Product save(Product product);

	// soft delete 제외하고 조회
	Optional<Product> findByIdAndDeletedAtIsNull(UUID id);

	// 전체 조회
	Page<Product> findAll(Pageable pageable);

	Page<Product> findAllByCategoryAndDeletedAtIsNull(String category, Pageable pageable);
}