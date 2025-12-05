package com.high.product.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.high.product.domain.model.Limited_Product;
import com.high.product.domain.repository.Limited_ProductRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class Limited_ProductRepositoryAdapter implements Limited_ProductRepository {

	private final JpaLimited_ProductRepository jpaLimitedProductRepository;



	@Override
	public Optional<Limited_Product> findById(UUID id) {
		return jpaLimitedProductRepository.findById(id);
	}

	@Override
	public Limited_Product save(Limited_Product product) {
		return jpaLimitedProductRepository.save(product);
	}

	@Override
	public Page<Limited_Product> findAll(Pageable pageable) {
		return jpaLimitedProductRepository.findAll(pageable);
	}

	@Override
	public Page<Limited_Product> findByCategory(String category, Pageable pageable) {
		return jpaLimitedProductRepository.findByCategory(category, pageable);
	}
}
