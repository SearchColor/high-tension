package com.high.user.application.dto.response;

import com.high.user.domain.entity.User;

import java.util.UUID;

/**
 * 내부 서비스 간 통신용 사용자 정보 Response DTO
 * <p>
 * Order/Payment/Coupon Service가 User 정보를 조회할 때 사용
 * </p>
 *
 * @param userId 사용자 고유 ID
 * @param email 이메일
 * @param name 사용자 이름
 * @param role 권한 (USER, SELLER, MASTER)
 * @param isActive 활성화 여부
 * @param phoneNumber 전화번호
 * @param deliveryAddress 배송 주소
 * @param detailAddress 상세 주소
 */
public record InternalUserResponse(
        UUID userId,
        String email,
        String name,
        String role,
        Boolean isActive,
        String phoneNumber,
        String deliveryAddress,
        String detailAddress
) {
    /**
     * User Entity에서 InternalUserResponse로 변환
     *
     * @param user User Entity
     * @return InternalUserResponse DTO
     */
    public static InternalUserResponse from(User user) {
        return new InternalUserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                user.getIsActive(),
                user.getPhoneNumber(),
                user.getDeliveryAddress(),
                user.getDetailAddress()
        );
    }
}
