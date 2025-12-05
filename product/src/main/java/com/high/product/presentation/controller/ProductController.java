package com.high.product.presentation.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.high.product.application.dto.request.LimitedProductCreateRequest;
import com.high.product.application.dto.request.ProductCreateRequest;
import com.high.product.application.dto.request.ProductUpdateRequest;
import com.high.product.application.dto.response.LimitedProductResponse;
import com.high.product.application.dto.response.ProductResponse;
import com.high.product.application.service.ProductService;
import com.library.jpa.response.PageResponse;
import com.library.module.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	// 일반상품 생성
	@PostMapping("/products")
	public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
		@RequestBody @Valid ProductCreateRequest request) {

		ProductResponse response = productService.createProduct(request);
		return new ResponseEntity<>(
			ApiResponse.success(response, "상품이 성공적으로 등록되었습니다."),
			HttpStatus.CREATED
		);
	}

	// 일반상품 ID로 단건 조회
	@GetMapping("/products/{productId}")
	public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable UUID productId) {
		ProductResponse response = productService.getProductById(productId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// 일반상품 전체 조회(페이징 및 정렬 적용)
	@GetMapping("/products")
	public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "createdAt") String sort,
		@RequestParam(defaultValue = "desc") String direction // asc/desc
	) {

		Page<ProductResponse> pageResult = productService.getProducts(page, size, sort, direction);

		// PageResponse DTO로 변환 (공통 라이브러리 활용)
		PageResponse<ProductResponse> response = PageResponse.fromPage(
			pageResult,
			sort,
			"asc".equalsIgnoreCase(direction)
		);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// 일반상품 카테고리별 조회
	@GetMapping("/products/category")
	public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProductsByCategory(
		@RequestParam String category,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "createdAt") String sort,
		@RequestParam(defaultValue = "desc") String direction
	) {

		Page<ProductResponse> pageResult = productService.getProductsByCategory(category, page, size, sort, direction);

		PageResponse<ProductResponse> response = PageResponse.fromPage(
			pageResult,
			sort,
			"asc".equalsIgnoreCase(direction)
		);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// 일반상품 수정
	@PutMapping("/products/{productId}")
	public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
		@PathVariable UUID productId,
		@RequestBody @Valid ProductUpdateRequest request) {

		ProductResponse response = productService.updateProduct(productId, request);
		return ResponseEntity.ok(ApiResponse.success(response, "상품 정보가 성공적으로 수정되었습니다."));
	}

	// 일반상품 삭제
	@DeleteMapping("/products/{productId}")
	public ResponseEntity<ApiResponse<?>> deleteProduct(@PathVariable UUID productId) {
		productService.deleteProduct(productId);
		return ResponseEntity.ok(ApiResponse.success("상품이 성공적으로 삭제되었습니다."));
	}

	// 한정상품 등록
	@PostMapping("/limited-products")
	public ResponseEntity<ApiResponse<LimitedProductResponse>> createLimitedProduct(
		@RequestBody @Valid LimitedProductCreateRequest request) {

		LimitedProductResponse response = productService.createLimitedProduct(request);

		return new ResponseEntity<>(
			ApiResponse.success(response, "한정 상품이 성공적으로 등록되었습니다."),
			HttpStatus.CREATED
		);
	}

	// 한정상품 단건 조회
	@GetMapping("/limited-products/{limitedProductId}")
	public ResponseEntity<ApiResponse<LimitedProductResponse>> getLimitedProductById(
		@PathVariable UUID limitedProductId) {
		LimitedProductResponse response = productService.getLimitedProductById(limitedProductId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// 한정상품 전체 조회
	@GetMapping("/limited-products")
	public ResponseEntity<ApiResponse<PageResponse<LimitedProductResponse>>> getAllLimitedProducts(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "createdAt") String sort,
		@RequestParam(defaultValue = "desc") String direction // asc/desc
	) {

		Page<LimitedProductResponse> pageResult = productService.getAllLimitedProducts(page, size, sort, direction);

		PageResponse<LimitedProductResponse> response = PageResponse.fromPage(
			pageResult,
			sort,
			"asc".equalsIgnoreCase(direction)
		);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// 한정상품 카테고리별 조회
	@GetMapping("/limited-products/category")
	public ResponseEntity<ApiResponse<PageResponse<LimitedProductResponse>>> getLimitedProductsByCategory(
		@RequestParam String category,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "createdAt") String sort,
		@RequestParam(defaultValue = "desc") String direction // asc/desc
	) {

		Page<LimitedProductResponse> pageResult = productService.getLimitedProductsByCategory(category, page, size,
			sort, direction);

		PageResponse<LimitedProductResponse> response = PageResponse.fromPage(
			pageResult,
			sort,
			"asc".equalsIgnoreCase(direction)
		);

		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
