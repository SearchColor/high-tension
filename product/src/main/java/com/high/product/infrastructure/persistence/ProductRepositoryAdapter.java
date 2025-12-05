package com.high.product.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.high.product.domain.model.Product;
import com.high.product.domain.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

	private final JpaProductRepository jpaProductRepository;

	@Override
	public Optional<Product> findById(UUID id) {
		return jpaProductRepository.findById(id);
	}

	@Override
	public Product save(Product product) {
		return jpaProductRepository.save(product);
	}

	@Override
	public Optional<Product> findByIdAndDeletedAtIsNull(UUID id) {
		return jpaProductRepository.findByIdAndDeletedAtIsNull(id);
	}

	@Override
	public Page<Product> findAll(Pageable pageable) {
		return jpaProductRepository.findAll(pageable);
	}

	@Override
	public Page<Product> findAllByCategoryAndDeletedAtIsNull(String category, Pageable pageable) {
		return jpaProductRepository.findAllByCategoryAndDeletedAtIsNull(category, pageable);
	}
}
