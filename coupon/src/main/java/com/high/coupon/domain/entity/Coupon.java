package com.high.coupon.domain.entity;

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
public class Coupon {

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
     * todo: 예외처리 임시 작성
     */
    private static void validateDates(LocalDateTime issueStartAt, LocalDateTime issueEndAt, LocalDateTime validUntil) {
        if (issueStartAt == null)
            throw new IllegalArgumentException("발행 시작일은 필수입니다.");
        if (issueEndAt != null && issueEndAt.isBefore(issueStartAt))
            throw new IllegalArgumentException("발행 종료일은 시작일 이후여야 합니다.");
        if (validUntil.isBefore(issueStartAt))
            throw new IllegalArgumentException("유효기간 종료일은 발행 시작일 이후여야 합니다.");
    }
}
