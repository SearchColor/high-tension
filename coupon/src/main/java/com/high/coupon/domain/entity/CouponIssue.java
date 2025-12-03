package com.high.coupon.domain.entity;

import com.high.coupon.domain.exception.CouponAlreadyUsedException;
import com.high.coupon.domain.exception.CouponExpiredException;
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
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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


    /**
     * 주문 / 결제 취소 시 쿠폰 복원
     * 동일한 유효기간 이내에서만 복원 가능
     */
    public void restoreCoupon(LocalDateTime now) {

        if (Boolean.FALSE.equals(this.isUsed) || this.usedAt == null) {
            // 이미 사용 가능 상태이거나, 사용 기록이 없는 쿠폰은 종료 (ex. 사용 실패 후 재시도)
            log.info("Coupon {} is already unused or has no used record.", this.id);
            return;
        }

        // 유효기간 체크 (동일한 유효기간 이내에서만 복원 가능)
        if (now.isAfter(this.validEndAt)) {
            // 유효기간이 만료된 후에는 복원되지 않음
            throw new CouponExpiredException();
        }

        this.isUsed = false; // true -> false 복원
        this.usedAt = null;
    }

}