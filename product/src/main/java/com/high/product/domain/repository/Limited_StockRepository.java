package com.high.product.domain.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.high.product.domain.model.Limited_Product_Stock;

public interface Limited_StockRepository extends JpaRepository<Limited_Product_Stock, UUID> {


	// 전체 조회 (페이징/정렬)
	Page<Limited_Product_Stock> findAll(Pageable pageable);

	// 한정상품이 존재하는지 확인
	boolean existsBylimitedProductId(UUID limittedProductId);
}
