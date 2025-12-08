package com.high.product.infrastructure.kafka.producer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.high.product.exception.InsufficientStockException;
import com.high.product.exception.ProductErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final RetryTemplate retryTemplate;
	private final ObjectMapper objectMapper;
	private final ExecutorService executorService;

	/**
	 * 비동기 전송 + 내부적으로 RetryTemplate로 재시도(블로킹 호출을 별도 스레드에서 수행)
	 * 성공하면 정상 종료. 실패하면 RuntimeException 던짐.
	 */
	public CompletableFuture<Void> sendAsyncWithRetry(String topic, Object payload) {
		String payloadJson;
		try {
			payloadJson = objectMapper.writeValueAsString(payload);
		} catch (Exception e) {
			CompletableFuture<Void> failed = new CompletableFuture<>();
			failed.completeExceptionally(e);
			return failed;
		}

		return CompletableFuture.runAsync(() -> {
			try {
				// RetryTemplate 내에서 동기(send + get)를 호출하여 result 확인
				retryTemplate.execute(context -> {
					try {
						// kafkaTemplate.send(...).get(timeout) 으로 전송 결과 확인
						kafkaTemplate.send(topic, payloadJson).get(5, TimeUnit.SECONDS);
						log.info("[KafkaResultPublisher] send success topic={} payload={}", topic, payloadJson);
						return null;
					} catch (Exception e) {
						log.warn("[KafkaResultPublisher] send attempt failed topic={} attempt={} error={}",
							topic, context.getRetryCount() + 1, e.toString());
						throw new InsufficientStockException(ProductErrorCode.INSUFFICIENT_STOCK);
					}
				});
			} catch (Exception finalEx) {
				log.error("[KafkaResultPublisher] All retries exhausted for topic={} payload={}", topic, payloadJson,
					finalEx);
				throw new InsufficientStockException(ProductErrorCode.INSUFFICIENT_STOCK);
			}
		}, executorService);
	}

	/**
	 * 동기적으로 전송(호출자가 쓰레드 차단 OK 시 사용) - 재시도 없음
	 */
	public void sendSyncNoRetry(String topic, Object payload) {
		try {
			String payloadJson = objectMapper.writeValueAsString(payload);
			kafkaTemplate.send(topic, payloadJson).get(5, TimeUnit.SECONDS);
		} catch (Exception e) {
			throw new InsufficientStockException(ProductErrorCode.INSUFFICIENT_STOCK);
		}
	}
}
