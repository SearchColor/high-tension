package com.high.product.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.high.product.domain.model.Product_Stock;
import com.high.product.domain.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockRepositoryAdapter implements StockRepository {

	private final JpaStockRepository jpaStockRepository;

	@Override
	public Optional<Product_Stock> findById(UUID id) {
		return jpaStockRepository.findById(id);
	}

	@Override
	public Product_Stock save(Product_Stock stock) {
		return jpaStockRepository.save(stock);
	}

	@Override
	public Page<Product_Stock> findAll(Pageable pageable) {
		return jpaStockRepository.findAll(pageable);
	}

	@Override
	public Optional<Product_Stock> findByProductId(UUID productId) {
		// JpaStockRepository에 정의된 메서드 사용
		return jpaStockRepository.findByProductId(productId);
	}

	@Override
	public boolean existsByProductId(UUID productId) {
		// JpaStockRepository에 정의된 메서드 사용
		return jpaStockRepository.existsByProductId(productId);
	}
}
