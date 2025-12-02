package com.high.coupon.config;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

/**
 * 개발용 임시 값 config -> 유저 쪽 개발되면 삭제
 */
@Component
public class TestAuditorAware implements AuditorAware<UUID> {

    private static final UUID TEST_USER_ID =
            UUID.fromString("10000000-0000-0000-0000-00000000000");

    @Override
    public Optional<UUID> getCurrentAuditor() {
        return Optional.of(TEST_USER_ID); // 테스트 유저 임시값 -> UUID 변경 필요
    }
}
