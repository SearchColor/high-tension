package com.high.user.infrastructure.scheduler;

import com.high.user.domain.repository.PasskeyChallengeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasskeyChallengeCleanupScheduler {

    private final PasskeyChallengeRepository challengeRepository;

    /**
     * 만료된 Challenge 정리 (10분마다 실행)
     */
    @Scheduled(fixedRate = 600000)  // 10분 = 600,000ms
    @Transactional
    public void cleanupExpiredChallenges() {
        log.debug("[Scheduler] Cleaning up expired passkey challenges");

        LocalDateTime now = LocalDateTime.now();
        challengeRepository.deleteExpiredChallenges(now);

        log.debug("[Scheduler] Expired passkey challenges cleaned up");
    }
}
