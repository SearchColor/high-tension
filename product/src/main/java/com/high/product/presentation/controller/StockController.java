package com.high.product.presentation.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.high.product.application.dto.request.LimitedStockCreateRequest;
import com.high.product.application.dto.request.StockCreateRequest;
import com.high.product.application.dto.request.StockUpdateRequest;
import com.high.product.application.dto.response.LimitedStockResponse;
import com.high.product.application.dto.response.StockResponse;
import com.high.product.application.service.StockService;
import com.library.jpa.response.PageResponse;
import com.library.module.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StockController {

	private final StockService stockService;

	// 재고 등록 (초기 수량 설정)
	@PostMapping("/products/stocks")
	@PreAuthorize("hasAnyRole('SELLER','MASTER')")
	public ResponseEntity<ApiResponse<StockResponse>> createStock(
		@RequestBody @Valid StockCreateRequest request) {

		StockResponse response = stockService.createStock(request);
		return new ResponseEntity<>(
			ApiResponse.success(response, "재고 정보가 성공적으로 등록되었습니다."),
			HttpStatus.CREATED
		);
	}

	// ID로 단건 조회
	// @GetMapping("/products/stocks/{productId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<StockResponse>> getStockById(@PathVariable UUID productId) {
		StockResponse response = stockService.getStockById(productId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// 페이징 및 정렬 적용 전체 조회
	@GetMapping("/products/stocks")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<PageResponse<StockResponse>>> getStocks(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "createdAt") String sort,
		@RequestParam(defaultValue = "desc") String direction
	) {
		Page<StockResponse> pageResult = stockService.getALLStocks(page, size, sort, direction);

		PageResponse<StockResponse> response = PageResponse.fromPage(
			pageResult,
			sort,
			"asc".equalsIgnoreCase(direction)
		);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// 한정상품 재고 등록
	@PostMapping("/limited-products/stocks")
	@PreAuthorize("hasAnyRole('SELLER','MASTER')")
	public ResponseEntity<ApiResponse<LimitedStockResponse>> createLimitedStock(
		@RequestBody @Valid LimitedStockCreateRequest request) {

		LimitedStockResponse response = stockService.createLimitedStock(request);
		return new ResponseEntity<>(
			ApiResponse.success(response, "한정상품 재고 정보가 성공적으로 등록되었습니다."),
			HttpStatus.CREATED
		);
	}

	// ID로 단건 조회
	@GetMapping("/limited-products/stocks/{limitedProductId}")
	@PreAuthorize("hasAnyRole('SELLER','MASTER')")
	public ResponseEntity<ApiResponse<LimitedStockResponse>> getStockByLimitedProductById(
		@PathVariable UUID limitedProductId) {
		LimitedStockResponse response = stockService.getStockByLimitedProductById(limitedProductId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// 페이징 및 정렬 적용 전체 조회
	@GetMapping("/limited-products/stocks")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<PageResponse<LimitedStockResponse>>> getAllLimitedStocks(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "createdAt") String sort,
		@RequestParam(defaultValue = "desc") String direction
	) {
		Page<LimitedStockResponse> pageResult = stockService.getAllLimitedStocks(page, size, sort, direction);

		PageResponse<LimitedStockResponse> response = PageResponse.fromPage(
			pageResult,
			sort,
			"asc".equalsIgnoreCase(direction)
		);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// 일반상품 재고 차감
	@PostMapping("/products/stocks/reduce")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<StockResponse>> stockReduce(
		@RequestBody StockUpdateRequest request
	) {

		StockResponse response = stockService.reduceStock(request.productId(), request.quantity());
		return new ResponseEntity<>(
			ApiResponse.success(response, "일반 상품 재고가 성공적으로 차감되었습니다."),
			HttpStatus.CREATED
		);
	}

}
