package com.high.product.application.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.high.product.application.dto.request.LimitedStockCreateRequest;
import com.high.product.application.dto.request.StockCreateRequest;
import com.high.product.application.dto.response.LimitedStockResponse;
import com.high.product.application.dto.response.StockResponse;
import com.high.product.application.exception.ProductException;
import com.high.product.domain.model.Limited_Product;
import com.high.product.domain.model.Limited_Product_Stock;
import com.high.product.domain.model.Product;
import com.high.product.domain.model.Product_Stock;
import com.high.product.domain.repository.Limited_ProductRepository;
import com.high.product.domain.repository.Limited_StockRepository;
import com.high.product.domain.repository.ProductRepository;
import com.high.product.domain.repository.StockRepository;
import com.high.product.exception.ProductErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StockService {

	private final StockRepository stockRepository;
	private final Limited_StockRepository limitedStockRepository;
	private final ProductRepository productRepository;
	private final Limited_ProductRepository limitedProductRepository;

	// 일반상품 재고 등록
	public StockResponse createStock(StockCreateRequest request) {

		// 해당 상품이 있는지 확인
		Product product = productRepository.findById(request.productId())
			.orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

		// soft delete 여부 확인
		if (product.isDeleted()) {
			throw new ProductException(ProductErrorCode.PRODUCT_ALREADY_DELETED);
		}

		// 해당 상품의 재고가 있는지 확인(상품 1개당 1개의 재고 등록가능)
		if (stockRepository.existsByProductId(request.productId())) {
			throw new ProductException(ProductErrorCode.DUPLICATE_STOCK);
		}

		Product_Stock stock = Product_Stock.createProduct(
			request.productId(),
			request.quantity()
		);

		Product_Stock savedStock = stockRepository.save(stock);
		return StockResponse.from(savedStock);
	}

	// 일반상품 재고 단건 조회
	@Transactional(readOnly = true)
	public StockResponse getStockById(UUID productId) {
		Product_Stock stock = stockRepository.findByProductId(productId)
			.orElseThrow(() -> new ProductException(ProductErrorCode.STOCK_NOT_FOUND));

		return StockResponse.from(stock);
	}

	// 일반상품 재고 전체 조회(페이징 및 정렬 적용)
	@Transactional(readOnly = true)
	public Page<StockResponse> getALLStocks(int page, int size, String sort, String direction) {
		// Pageable 객체 생성
		Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
		Sort finalSort = Sort.by(sortDirection, sort);

		Pageable pageable = PageRequest.of(page - 1, size, finalSort); // 1-based page를 0-based로 변환

		return stockRepository.findAll(pageable)
			.map(StockResponse::from); // Page<Product>를 Page<ProductResponse>로 변환
	}

	// 한정상품 재고 등록
	public LimitedStockResponse createLimitedStock(LimitedStockCreateRequest request) {

		// 해당 상품이 있는지 확인
		Limited_Product product = limitedProductRepository.findById(request.limitedProductId())
			.orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

		// 해당 상품의 재고가 있는지 확인(상품 1개에 1개의 재고 등록가능)
		if (limitedStockRepository.existsBylimitedProductId(request.limitedProductId())) {
			throw new ProductException(ProductErrorCode.DUPLICATE_STOCK);
		}

		Limited_Product_Stock limitedStock = Limited_Product_Stock.createStock(
			request.limitedProductId(),
			request.quantity()
		);

		Limited_Product_Stock savedStock = limitedStockRepository.save(limitedStock);

		return LimitedStockResponse.from(savedStock);
	}

	// 한정상품Id로 재고 단건 조회
	public LimitedStockResponse getStockByLimitedProductById(UUID limitedProductId) {

		Limited_Product_Stock limitedProduct = limitedStockRepository.findById(limitedProductId)
			.orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

		return LimitedStockResponse.from(limitedProduct);
	}

	// 한정상품 재고 전체 조회 (페이징 & 정렬)
	public Page<LimitedStockResponse> getAllLimitedStocks(int page, int size, String sort, String direction) {

		Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
		Sort finalSort = Sort.by(sortDirection, sort);

		Pageable pageable = PageRequest.of(page - 1, size, finalSort);

		return limitedStockRepository.findAll(pageable)
			.map(LimitedStockResponse::from);
	}
}
