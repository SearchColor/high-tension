package com.high.product.application.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.high.product.application.dto.request.LimittedStockCreateRequest;
import com.high.product.application.dto.request.StockCreateRequest;
import com.high.product.application.dto.response.LimittedStockResponse;
import com.high.product.application.dto.response.StockResponse;
import com.high.product.application.exception.ProductException;
import com.high.product.domain.model.Limitted_Product;
import com.high.product.domain.model.Limitted_Product_Stock;
import com.high.product.domain.model.Product;
import com.high.product.domain.model.Product_Stock;
import com.high.product.domain.repository.Limitted_ProductRepository;
import com.high.product.domain.repository.Limitted_StockRepository;
import com.high.product.domain.repository.ProductRepository;
import com.high.product.domain.repository.StockRepository;
import com.high.product.exception.ProductErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StockService {

	private final StockRepository stockRepository;
	private final Limitted_StockRepository limittedStockRepository;
	private final ProductRepository productRepository;
	private final Limitted_ProductRepository limittedProductRepository;

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
	public LimittedStockResponse createLimittedStock(LimittedStockCreateRequest request) {

		// 해당 상품이 있는지 확인
		Limitted_Product product = limittedProductRepository.findById(request.limittedProductId())
			.orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

		// 해당 상품의 재고가 있는지 확인(상품 1개에 1개의 재고 등록가능)
		if (limittedStockRepository.existsBylimittedProductId(request.limittedProductId())) {
			throw new ProductException(ProductErrorCode.DUPLICATE_STOCK);
		}

		Limitted_Product_Stock limittedStock = Limitted_Product_Stock.createStock(
			request.limittedProductId(),
			request.quantity()
		);

		Limitted_Product_Stock savedStock = limittedStockRepository.save(limittedStock);

		return LimittedStockResponse.from(savedStock);
	}

	// 한정상품Id로 재고 단건 조회
	public LimittedStockResponse getStockByLimittedProductById(UUID limittedProductId) {

		Limitted_Product_Stock limittedProduct = limittedStockRepository.findById(limittedProductId)
			.orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

		return LimittedStockResponse.from(limittedProduct);
	}

	// 한정상품 재고 전체 조회 (페이징 & 정렬)
	public Page<LimittedStockResponse> getAllLimittedStocks(int page, int size, String sort, String direction) {

		Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
		Sort finalSort = Sort.by(sortDirection, sort);

		Pageable pageable = PageRequest.of(page - 1, size, finalSort);

		return limittedStockRepository.findAll(pageable)
			.map(LimittedStockResponse::from);
	}
}
