package com.high.product.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.high.product.domain.model.Limited_Product;

@Repository
public interface JpaLimited_ProductRepository extends JpaRepository<Limited_Product, UUID> {

	Page<Limited_Product> findByCategory(String category, Pageable pageable);
}
