package com.high.product.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.high.product.application.exception.ProductException;
import com.high.product.exception.ProductErrorCode;
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
@Table(name = "p_limited_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Limited_Product extends BaseCreateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private int price;

	@Column(nullable = false)
	private String category;

	@Column(nullable = false)
	private UUID seller;

	@Column(name = "discount_rate", nullable = false)
	private int discountrate;

	// Instant 타입이 UTC의 정보를 가지고 있어 안전하지만
	// config-repo에서 각 서비스의 JDBC Url의 TimeZone설정을 UTC로 했으므로 LocalDateTime 사용
	@Column(nullable = false)
	private LocalDateTime end;

	@Builder
	public Limited_Product(String name, int price, String category, UUID seller, int discountRate,
		LocalDateTime end) {

		// 할인율은 0% 미만이 될 수 없음
		if (discountRate < 0 || discountRate > 100) {
			throw new ProductException(ProductErrorCode.DISCOUNT_RATE_OUT_OF_RANGE);
		}

		this.name = name;
		this.price = price;
		this.category = category;
		this.seller = seller;
		this.discountrate = discountRate;
		this.end = end;
	}

	// 한정상품 생성 정적 메서드
	public static Limited_Product createProduct(String name, Integer price, String category, UUID seller,
		int discountrate, LocalDateTime end) {
		return Limited_Product.builder()
			.name(name)
			.price(price)
			.category(category)
			.seller(seller)
			.discountRate(discountrate)
			.end(end)
			.build();
	}
}
