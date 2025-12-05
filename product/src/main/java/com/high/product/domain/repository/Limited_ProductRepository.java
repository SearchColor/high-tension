package com.high.product.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.high.product.domain.model.Limited_Product;
import com.high.product.domain.model.Limited_Product_Stock;

public interface Limited_ProductRepository {

	Optional<Limited_Product> findById(UUID id);

	Limited_Product save(Limited_Product product);

	// 전체 조회
	Page<Limited_Product> findAll(Pageable pageable);

	// 카테고리별 조회 (soft delete 제외)
	Page<Limited_Product> findByCategory(String category, Pageable pageable);
}
