package com.high.product.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.high.product.domain.model.Limitted_Product_Stock;

import jakarta.validation.constraints.NotBlank;

public interface Limitted_StockRepository extends JpaRepository<Limitted_Product_Stock, UUID> {


	// 전체 조회 (페이징/정렬)
	Page<Limitted_Product_Stock> findAll(Pageable pageable);

	// 한정상품이 존재하는지 확인
	boolean existsBylimittedProductId(UUID limittedProductId);
}
