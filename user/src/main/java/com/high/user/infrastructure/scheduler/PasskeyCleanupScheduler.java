package com.high.user.infrastructure.scheduler;

import com.high.user.domain.entity.Passkey;
import com.high.user.domain.repository.PasskeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasskeyCleanupScheduler {

    private final PasskeyRepository passkeyRepository;

    /**
     * Soft Delete 된지 30일이 지난 패스키 영구 삭제
     * 매일 자정 실행
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deleteExpiredPasskeys() {
        log.info("Passkey cleanup scheduler started");

        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        List<Passkey> expiredPasskeys = passkeyRepository.findByDeletedAtBefore(threshold);

        if (!expiredPasskeys.isEmpty()) {
            passkeyRepository.deleteAll(expiredPasskeys);
            log.info("Deleted {} expired passkeys", expiredPasskeys.size());
        } else {
            log.info("No expired passkeys found");
        }
    }
}
