package com.high.product.domain.model;

import java.util.UUID;

import com.library.jpa.common.entity.BaseEntity;

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
@Table(name = "p_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

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

	@Builder
	public Product(String name, int price, String category, UUID seller) {
		this.name = name;
		this.price = price;
		this.category = category;
		this.seller = seller;
	}

	// 일반상품 생성 정적 메서드
	public static Product createProduct(String name, Integer price, String category, UUID seller) {
		return Product.builder()
			.name(name)
			.price(price)
			.category(category)
			.seller(seller)
			.build();
	}

	// 일반상품 수정
	public void update(String name, Integer price, String category, UUID seller) {
		this.name = name;
		this.price = price;
		this.category = category;
		this.seller = seller;
	}
}
