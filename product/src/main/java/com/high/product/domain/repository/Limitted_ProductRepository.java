package com.high.product.domain.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.high.product.domain.model.Limitted_Product;

public interface Limitted_ProductRepository extends JpaRepository<Limitted_Product, UUID> {

	// 전체 조회
	Page<Limitted_Product> findAll(Pageable pageable);

	// 카테고리별 조회 (soft delete 제외)
	Page<Limitted_Product> findByCategory(String category, Pageable pageable);
}
