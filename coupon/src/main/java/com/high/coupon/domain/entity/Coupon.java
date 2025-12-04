package com.high.coupon.domain.entity;

import com.high.coupon.domain.exception.CouponInvalidDateException;
import com.high.coupon.domain.exception.CouponInvalidDiscountRateException;
import com.high.coupon.domain.exception.CouponInvalidIssuePeriodException;
import com.high.coupon.domain.exception.CouponInvalidValidUntilException;
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
    private BigDecimal discountRate;

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
        validateDiscountRate(discountRate);

        return Coupon.builder()
                .name(name)
                .description(description)
                .discountRate(discountRate)
                .totalQuantity(totalQuantity)
                .issueStartAt(issueStartAt)
                .issueEndAt(issueEndAt)
                .validUntil(validUntil)
                .build();
    }

    /**
     * 날짜 검증 메서드
     */
    private static void validateDates(LocalDateTime issueStartAt, LocalDateTime issueEndAt, LocalDateTime validUntil) {
        // null 체크
        if (issueStartAt == null || issueEndAt == null || validUntil == null) {
            throw new CouponInvalidDateException();
        }

        // 발행 기간 검증 (발행 종료일이 발행 시작일 보다 이전이면 안 됨)
        if (issueEndAt.isBefore(issueStartAt)) {
            throw new CouponInvalidIssuePeriodException();
        }

        // 유효기간 검증 (유효기간은 발행 종료 이후여야 함)
        if (validUntil.isBefore(issueEndAt)) {
            throw new CouponInvalidValidUntilException();
        }
    }

    /**
     * 할인율 범위 체크
     */
    private static void validateDiscountRate(BigDecimal discountRate) {
        if (discountRate == null
                || discountRate.compareTo(BigDecimal.ZERO) <= 0
                || discountRate.compareTo(BigDecimal.valueOf(100)) > 0
                || discountRate.scale() > 0) {
            throw new CouponInvalidDiscountRateException();
        }
    }
}
