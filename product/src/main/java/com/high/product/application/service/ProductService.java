package com.high.product.application.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.high.product.application.dto.request.LimittedProductCreateRequest;
import com.high.product.application.dto.request.ProductCreateRequest;
import com.high.product.application.dto.request.ProductUpdateRequest;
import com.high.product.application.dto.response.LimittedProductResponse;
import com.high.product.application.dto.response.ProductResponse;
import com.high.product.application.exception.ProductException;
import com.high.product.domain.model.Limitted_Product;
import com.high.product.domain.model.Product;
import com.high.product.domain.repository.Limitted_ProductRepository;
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
	private final Limitted_ProductRepository limitted_ProductRepository;

	// 일반상품 생성
	public ProductResponse createProduct(ProductCreateRequest request) {

		// 상풍명 중복 확인
		if (productRepository.existsByName(request.name())) {
			throw new ProductException(ProductErrorCode.DUPLICATE_PRODUCT_NAME);
		}

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
	public Page<ProductResponse> getProductsByCategory(String category, int page, int size, String sort,
		String direction) {
		Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
		Sort finalSort = Sort.by(sortDirection, sort);

		Pageable pageable = PageRequest.of(page - 1, size, finalSort);

		return productRepository.findAllByCategoryAndDeletedAtIsNull(category, pageable)
			.map(ProductResponse::from);
	}

	// 일반상품 수정
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
	public LimittedProductResponse createLimittedProduct(LimittedProductCreateRequest request) {

		// 상풍명 중복 확인
		if (productRepository.existsByName(request.name())) {
			throw new ProductException(ProductErrorCode.DUPLICATE_PRODUCT_NAME);
		}

		Limitted_Product limittedProduct = Limitted_Product.createProduct(
			request.name(),
			request.price(),
			request.category(),
			request.seller(),
			request.discountRate(),
			request.end()
		);

		Limitted_Product savedProduct = limitted_ProductRepository.save(limittedProduct);
		return LimittedProductResponse.from(savedProduct);
	}

	// 한정상품 단건 조회
	@Transactional(readOnly = true)
	public LimittedProductResponse getLimittedProductById(UUID limittedProductId) {
		Limitted_Product limittedProduct = limitted_ProductRepository.findById(limittedProductId)
			.orElseThrow(() -> new CustomException(CommonErrorCode.NOT_FOUND));
		return LimittedProductResponse.from(limittedProduct);
	}

	// 한정상품 전제 조회(페이징 & 정렬)
	@Transactional(readOnly = true)
	public Page<LimittedProductResponse> getAllLimittedProducts(int page, int size, String sort, String direction) {

		Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
		Sort finalSort = Sort.by(sortDirection, sort);

		Pageable pageable = PageRequest.of(page - 1, size, finalSort);

		return limitted_ProductRepository.findAll(pageable)
			.map(LimittedProductResponse::from);
	}

	// 한정상품 카테고리별 조회(페이징 & 정렬)
	@Transactional(readOnly = true)
	public Page<LimittedProductResponse> getLimittedProductsByCategory(String category, int page, int size, String sort,
		String direction) {

		Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
		Sort finalSort = Sort.by(sortDirection, sort);

		Pageable pageable = PageRequest.of(page - 1, size, finalSort);

		return limitted_ProductRepository.findByCategory(category, pageable)
			.map(LimittedProductResponse::from);
	}
}
