package com.high.product.application.service;

import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.high.product.application.dto.request.LimitedProductCreateRequest;
import com.high.product.application.dto.request.ProductCreateRequest;
import com.high.product.application.dto.request.ProductUpdateRequest;
import com.high.product.application.dto.response.LimitedProductResponse;
import com.high.product.application.dto.response.ProductResponse;
import com.high.product.application.exception.ProductException;
import com.high.product.domain.model.Limited_Product;
import com.high.product.domain.model.Product;
import com.high.product.domain.repository.Limited_ProductRepository;
import com.high.product.domain.repository.ProductRepository;
import com.high.product.exception.ProductErrorCode;
import com.library.module.exception.CommonErrorCode;
import com.library.module.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

	private final ProductRepository productRepository;
	private final Limited_ProductRepository limited_ProductRepository;

	// 일반상품 생성
	@CacheEvict(
		cacheNames = "product",
		key = "#result.id()",
		condition = "#result != null"
	)
	public ProductResponse createProduct(ProductCreateRequest request) {

		Product product = Product.createProduct(
			request.name(),
			request.price(),
			request.category(),
			request.seller()
		);

		Product savedProduct = productRepository.save(product);
		return ProductResponse.from(savedProduct);
	}

	// 일반상품 단건 조회
	@Transactional(readOnly = true)
	@Cacheable(
		cacheNames = "product",
		key = "#productId"
	)
	public ProductResponse getProductById(UUID productId) {
		Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
			.orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

		return ProductResponse.from(product);
	}

	// 일반상품 전체 조회(페이징 및 정렬 적용)
	@Transactional(readOnly = true)
	public Page<ProductResponse> getProducts(int page, int size, String sort, String direction) {
		// Pageable 객체 생성
		Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
		Sort finalSort = Sort.by(sortDirection, sort);

		Pageable pageable = PageRequest.of(page - 1, size, finalSort); // 1-based page를 0-based로 변환

		return productRepository.findAll(pageable)
			.map(ProductResponse::from); // Page<Product>를 Page<ProductResponse>로 변환
	}

	// 일반상품 카테고리별 조회
	@Transactional(readOnly = true)
	@Cacheable(
		value = "productsByCategory",
		key = "{#category, #page, #size, #sort, #direction}",
		unless = "#result == null"
	)
	public Page<ProductResponse> getProductsByCategory(String category, int page, int size, String sort,
		String direction) {
		Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
		Sort finalSort = Sort.by(sortDirection, sort);

		Pageable pageable = PageRequest.of(page - 1, size, finalSort);

		return productRepository.findAllByCategoryAndDeletedAtIsNull(category, pageable)
			.map(ProductResponse::from);
	}

	// 일반상품 수정
	@CacheEvict(
		cacheNames = "product",
		key = "#productId"
	)
	public ProductResponse updateProduct(UUID productId, ProductUpdateRequest request) {
		// 상품 존재 확인
		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

		// soft delete 여부 확인
		if (product.isDeleted()) {
			throw new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND);
		}

		product.update(request.name(), request.price(), request.category(), request.seller());

		return ProductResponse.from(product);
	}

	// 일반상품 삭제(임시)
	@CacheEvict(
		cacheNames = "product",
		key = "#productId"
	)
	public void deleteProduct(UUID productId) {
		// 상품 존재 확인
		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

		// soft delete 여부 확인
		if (product.isDeleted()) {
			throw new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND);
		}

		product.softDelete(product.getId());
	}

	// 한정상품 등록
	@CacheEvict(
		cacheNames = "limitedProduct",
		key = "#result.id()",
		condition = "#result != null"
	)
	public LimitedProductResponse createLimitedProduct(LimitedProductCreateRequest request) {


		Limited_Product limitedProduct = Limited_Product.createProduct(
			request.name(),
			request.price(),
			request.category(),
			request.seller(),
			request.discountRate(),
			request.end()
		);

		Limited_Product savedProduct = limited_ProductRepository.save(limitedProduct);
		return LimitedProductResponse.from(savedProduct);
	}

	// 한정상품 단건 조회
	@Transactional(readOnly = true)
	@Cacheable(
		cacheNames = "limitedProduct",
		key = "#limitedProductId"
	)
	public LimitedProductResponse getLimitedProductById(UUID limitedProductId) {
		Limited_Product limitedProduct = limited_ProductRepository.findById(limitedProductId)
			.orElseThrow(() -> new CustomException(CommonErrorCode.NOT_FOUND));
		return LimitedProductResponse.from(limitedProduct);
	}

	// 한정상품 전제 조회(페이징 & 정렬)
	@Transactional(readOnly = true)
	public Page<LimitedProductResponse> getAllLimitedProducts(int page, int size, String sort, String direction) {

		Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
		Sort finalSort = Sort.by(sortDirection, sort);

		Pageable pageable = PageRequest.of(page - 1, size, finalSort);

		return limited_ProductRepository.findAll(pageable)
			.map(LimitedProductResponse::from);
	}

	// 한정상품 카테고리별 조회(페이징 & 정렬)
	@Transactional(readOnly = true)
	@Cacheable(
		value = "limitedProductsByCategory",
		key = "{#category, #page, #size, #sort, #direction}",
		unless = "#result == null"
	)
	public Page<LimitedProductResponse> getLimitedProductsByCategory(String category, int page, int size, String sort,
		String direction) {

		Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
		Sort finalSort = Sort.by(sortDirection, sort);

		Pageable pageable = PageRequest.of(page - 1, size, finalSort);

		return limited_ProductRepository.findByCategory(category, pageable)
			.map(LimitedProductResponse::from);
	}
}
