package com.high.coupon.domain.entity;

import com.high.coupon.domain.exception.CouponAlreadyUsedException;
import com.high.coupon.domain.exception.CouponIssuePeriodInvalidException;
import com.high.coupon.domain.exception.CouponNotOwnedException;
import com.high.coupon.domain.exception.CouponNotValidPeriodException;
import com.library.jpa.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
@Table(name = "p_coupon_issue",
    uniqueConstraints = {
        @UniqueConstraint(
                columnNames = {"coupon_id", "user_id"}
        )
})
public class CouponIssue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    private Boolean isUsed = false;

    private LocalDateTime usedAt;

    @Column(nullable = false)
    private LocalDateTime validStartAt;

    @Column(nullable = false)
    private LocalDateTime validEndAt;


    /**
     * 쿠폰 발급
     */
    public static CouponIssue issueCoupon(Coupon coupon, UUID userId, LocalDateTime now){

        validateIssuePeriod(coupon, now);

        return CouponIssue.builder()
                .coupon(coupon)
                .userId(userId)
                .issuedAt(now)
                .validStartAt(now)
                .validEndAt(coupon.getValidUntil()) // 쿠폰 유효 기간
                .isUsed(false)
                .build();
    }

    // 발급 기간 체크 메서드
    private static void validateIssuePeriod(Coupon coupon, LocalDateTime now) {
        if (now.isBefore(coupon.getIssueStartAt()) || now.isAfter(coupon.getIssueEndAt())) {
            throw new CouponIssuePeriodInvalidException();
        }
    }

    /**
     * 쿠폰 사용 처리
     * todo 사용자 인증 처리 정리 필요 (service)
     */
    public void useCoupon(UUID userId, LocalDateTime now){
        if (Boolean.TRUE.equals(this.isUsed)){
            throw new CouponAlreadyUsedException();
        }

        if (!this.userId.equals(userId)){
            throw new CouponNotOwnedException();
        }

        if (now.isBefore(validStartAt) || now.isAfter(validEndAt)){
            throw new CouponNotValidPeriodException();
        }

        this.isUsed = true;
        this.usedAt = now;
    }



    // todo ---- 할인 금액 계산 쿠폰쪽에서 필요할지 피드백 필요
    /**
     * 쿠폰 할인률 계산
     */
    public BigDecimal calculateDiscountPrice(BigDecimal originalPrice){
        BigDecimal rate = coupon.getDiscountRate();
        return originalPrice
                .multiply(rate) // 원가 x rate 10000 x 0.1 -> 1000원 할인
                .setScale(0, RoundingMode.HALF_UP); // todo 소수점 처리 방식 검토 필요 DOWN, HALF_UP
    }

    /**
     * 쿠폰 적용 후 결제 금액
     */
    public BigDecimal calculateFinalPrice(BigDecimal originalPrice) {
        BigDecimal discount = calculateDiscountPrice(originalPrice);
        return originalPrice.subtract(discount).max(BigDecimal.ZERO);
    }
}