package com.high.product.infrastructure.redis;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.high.product.application.port.DistributedLockPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDistributedLockAdapter implements DistributedLockPort {

	private final RedissonClient redissonClient;
	private final PlatformTransactionManager transactionManager;

	@Override
	public void executeWithMultiLock(List<String> lockKeys, Runnable action) {

		// 락 키 정렬 (데드락 방지 핵심)
		List<String> sortedKeys = lockKeys.stream()
			.sorted()
			.toList();

		log.info("멀티 락 획득 시도 순서: {}", sortedKeys);

		List<RLock> locks = sortedKeys.stream()
			.map(redissonClient::getLock)
			.toList();

		try {
			// 순서대로 락 획득
			for (RLock lock : locks) {
				boolean acquired = lock.tryLock(5, 0, TimeUnit.SECONDS);
				if (!acquired) {
					log.error("분산 락 획득 실패: {}", lock.getName());
					throw new IllegalStateException("분산 락 획득 실패: " + lock.getName());
				}
			}

			// 단일 트랜잭션
			TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
			txTemplate.execute(status -> {
				action.run();
				return null;
			});

		} catch (Exception e) {
			log.error("멀티 분산락 처리 중 오류", e);
			throw new RuntimeException(e);
		} finally {
			// 역순 해제(처리 완료된 트랜잭션을 해제)
			for (int i = locks.size() - 1; i >= 0; i--) {
				RLock lock = locks.get(i);
				if (lock.isHeldByCurrentThread()) {
					lock.unlock();
					log.info("분산 락 해제 완료: {}", lock.getName());
				} else {
					log.warn("이미 만료되었거나 해제된 락입니다: {}", lock.getName());
				}
			}
		}
	}
}
