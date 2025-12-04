package com.high.product.domain.model;

import java.util.UUID;

import com.high.product.exception.ProductErrorCode;
import com.high.product.application.exception.ProductException;
import com.library.jpa.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_stock")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product_Stock extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "product_id", nullable = false, unique = true)
	private UUID productId;

	@Column(nullable = false)
	private int quantity;

	@Builder
	public Product_Stock(UUID productId, int quantity) {
		this.productId = productId;
		this.quantity = quantity;
	}


	// 재고 등록 정적 메서드
	public static Product_Stock createProduct(UUID productId, int quantity) {
		return Product_Stock.builder()
			.productId(productId)
			.quantity(quantity)
			.build();
	}

	// 재고 증가 (증가 / 복원)
	public void increase(int amount) {
		if (amount <= 0) {
			throw new ProductException(ProductErrorCode.NEGATIVE_STOCK_OPERATION);
		}
		this.quantity += amount;
	}

	// 재고 감소(주문)
	public void decrease(int amount) {
		if (amount <= 0) {
			throw new ProductException(ProductErrorCode.NEGATIVE_STOCK_OPERATION);
		}
		if (this.quantity < amount) {

			throw new ProductException(ProductErrorCode.INSUFFICIENT_STOCK);
		}
		this.quantity -= amount;
	}

	// 재고 수량 초기 등록/수정 (초기 재고 등록 시 사용)
	public void updateQuantity(int newQuantity) {
		if (newQuantity < 0) {
			throw new ProductException(ProductErrorCode.NEGATIVE_STOCK_OPERATION);
		}
		this.quantity = newQuantity;
	}
}
