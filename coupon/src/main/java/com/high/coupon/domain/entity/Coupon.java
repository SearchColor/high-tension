package com.high.coupon.domain.entity;

import com.high.coupon.domain.exception.CouponInvalidDateException;
import com.high.coupon.domain.exception.CouponInvalidDiscountRateException;
import com.library.jpa.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "p_coupon")
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountRate; // 0~1 범위 저장

    @Column(nullable = false)
    private Integer totalQuantity;

    @Column(nullable = false)
    private LocalDateTime issueStartAt;

    @Column(nullable = false)
    private LocalDateTime issueEndAt;

    @Column(nullable = false)
    private LocalDateTime validUntil;

    /**
     * 쿠폰 생성
     */
    public static Coupon createCoupon(String name, String description, BigDecimal discountRate,
            Integer totalQuantity, LocalDateTime issueStartAt, LocalDateTime issueEndAt, LocalDateTime validUntil){

        validateDates(issueStartAt, issueEndAt, validUntil);
        BigDecimal convertDiscountRate = convertAndValidateDiscountRate(discountRate);

        return Coupon.builder()
                .name(name)
                .description(description)
                .discountRate(convertDiscountRate) // 10(입력) -> 0.1 저장 (0~1) 할인률 범위
                .totalQuantity(totalQuantity)
                .issueStartAt(issueStartAt)
                .issueEndAt(issueEndAt)
                .validUntil(validUntil)
                .build();
    }

    /**
     * 날짜 검증 메서드
     * todo: 예외처리 추가 검토 필요
     */
    private static void validateDates(LocalDateTime issueStartAt, LocalDateTime issueEndAt, LocalDateTime validUntil) {
        if (issueStartAt == null || issueEndAt == null || validUntil == null) {
            throw new CouponInvalidDateException();
        }
        if (issueEndAt.isBefore(issueStartAt) || validUntil.isBefore(issueStartAt)) {
            throw new CouponInvalidDateException();
        }
    }

    private static BigDecimal convertAndValidateDiscountRate(BigDecimal rate) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new CouponInvalidDiscountRateException();
        }
        if (rate.scale() > 0){
            throw new CouponInvalidDiscountRateException();
        }

        return rate.divide(BigDecimal.valueOf(100));
    }
}
