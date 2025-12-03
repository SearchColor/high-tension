package com.high.payment.infrastrucutre.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

@Configuration
public class KafkaConfig {
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
		ConsumerFactory<String, String> consumerFactory) {

		ConcurrentKafkaListenerContainerFactory<String, String> factory =
			new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);

		// Consumer가 메시지를 받을 때, 한 번에 묶어서(Batch) 처리하지 않고 하나씩 처리하도록 설정
		factory.setBatchListener(false);

		return factory;
	}

	// --- Kafka Producer 설정 ---

	/**
	 * Outbox Relay에서 사용할 KafkaTemplate (Producer) 설정입니다.
	 */
	@Bean
	public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
		KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory);

		// JSON 직렬화를 위한 MessageConverter 설정 (선택 사항이나, 깔끔한 전송을 위해 사용)
		template.setMessageConverter(new StringJsonMessageConverter());

		return template;
	}

	/**
	 * Payment Service가 구독할 토픽을 정의합니다. (주문 요청)
	 */
	@Bean
	public NewTopic orderRequestedTopic() {
		// 토픽 이름은 Consumer에서 사용한 이름과 일치해야 합니다.
		return new NewTopic("order.requested", 3, (short) 1);
	}

	/**
	 * Payment Service가 발행할 토픽을 정의.(결제 결과)
	 */
	@Bean
	public NewTopic paymentCompletedTopic() {
		return new NewTopic("payment.completed", 3, (short) 1);
	}
}
