package com.high.product.domain.model;

import java.util.UUID;

import com.high.product.exception.ProductErrorCode;
import com.high.product.application.exception.ProductException;
import com.library.jpa.common.entity.BaseCreateEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_limitted_product_stock")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Limitted_Product_Stock extends BaseCreateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "limited_product_id", nullable = false, unique = true)
	private UUID limittedProductId;

	@Column(nullable = false)
	private int quantity;

	@Builder
	public Limitted_Product_Stock(UUID limittedproductId, int quantity) {
		// 재고 증감량은 0보다 커야 함
		if (quantity < 0) {
			throw new ProductException(ProductErrorCode.NEGATIVE_STOCK_OPERATION);
		}

		this.limittedProductId = limittedproductId;
		this.quantity = quantity;
	}

	// ⭐ 한정상품 재고 생성 정적 메서드
	public static Limitted_Product_Stock createStock(UUID limittedProductId, int quantity) {
		return Limitted_Product_Stock.builder()
			.limittedproductId(limittedProductId)
			.quantity(quantity)
			.build();
	}
}
