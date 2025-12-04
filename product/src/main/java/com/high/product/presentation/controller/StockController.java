package com.high.product.presentation.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.high.product.application.dto.request.LimittedStockCreateRequest;
import com.high.product.application.dto.request.StockCreateRequest;
import com.high.product.application.dto.response.LimittedStockResponse;
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
	public ResponseEntity<ApiResponse<StockResponse>> createStock(
		@RequestBody @Valid StockCreateRequest request) {

		StockResponse response = stockService.createStock(request);
		return new ResponseEntity<>(
			ApiResponse.success(response, "재고 정보가 성공적으로 등록되었습니다."),
			HttpStatus.CREATED
		);
	}

	// ID로 단건 조회
	@GetMapping("/products/stocks/{productId}")
	public ResponseEntity<ApiResponse<StockResponse>> getStockById(@PathVariable UUID productId) {
		StockResponse response = stockService.getStockById(productId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// 페이징 및 정렬 적용 전체 조회
	@GetMapping("/products/stocks")
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
	@PostMapping("/limitted-products/stocks")
	public ResponseEntity<ApiResponse<LimittedStockResponse>> createLimittedStock(
		@RequestBody @Valid LimittedStockCreateRequest request) {

		LimittedStockResponse response = stockService.createLimittedStock(request);
		return new ResponseEntity<>(
			ApiResponse.success(response, "한정상품 재고 정보가 성공적으로 등록되었습니다."),
			HttpStatus.CREATED
		);
	}

	// ID로 단건 조회
	@GetMapping("/limitted-products/stocks/{limittedProductId}")
	public ResponseEntity<ApiResponse<LimittedStockResponse>> getStockByLimittedProductById(
		@PathVariable UUID limittedProductId) {
		LimittedStockResponse response = stockService.getStockByLimittedProductById(limittedProductId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// 페이징 및 정렬 적용 전체 조회
	@GetMapping("/limitted-products/stocks")
	public ResponseEntity<ApiResponse<PageResponse<LimittedStockResponse>>> getAllLimittedStocks(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "createdAt") String sort,
		@RequestParam(defaultValue = "desc") String direction
	) {
		Page<LimittedStockResponse> pageResult = stockService.getAllLimittedStocks(page, size, sort, direction);

		PageResponse<LimittedStockResponse> response = PageResponse.fromPage(
			pageResult,
			sort,
			"asc".equalsIgnoreCase(direction)
		);

		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
