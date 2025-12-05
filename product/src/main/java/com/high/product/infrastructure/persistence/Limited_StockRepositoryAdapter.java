package com.high.product.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.high.product.domain.model.Limited_Product_Stock;
import com.high.product.domain.repository.Limited_StockRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class Limited_StockRepositoryAdapter implements Limited_StockRepository {

	private final JpaLimited_StockRepository jpaRepository;

	@Override
	public Optional<Limited_Product_Stock> findById(UUID id) {
		return jpaRepository.findById(id);
	}

	@Override
	public Limited_Product_Stock save(Limited_Product_Stock stock) {
		return jpaRepository.save(stock);
	}

	@Override
	public Page<Limited_Product_Stock> findAll(Pageable pageable) {
		// JpaRepository의 findAll 호출
		return jpaRepository.findAll(pageable);
	}

	@Override
	public boolean existsBylimitedProductId(UUID limittedProductId) {
		// JpaLimited_StockRepository의 existsByLimitedProductId 호출
		return jpaRepository.existsByLimitedProductId(limittedProductId);
	}
}
