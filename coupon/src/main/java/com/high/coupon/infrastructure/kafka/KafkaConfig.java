package com.high.coupon.infrastructure.kafka;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
@EnableKafka
public class KafkaConfig {

    // todo 성공 결과 필요할까? (Producer 유무)

    @Bean
    public ConsumerFactory<String, String> consumerFactory(){

        /**
         * Kafka ConsumerFactory 설정
         * - Consumer 인스턴스를 생성하는 역할
         * - KafkaListener가 실제로 메시지를 가져갈 때 필요한 설정들이 포함
         */
        // Kafka consumer 설정 값: KEY - VALUE String 기반으로 처리
        Map<String, Object> props = new HashMap<>();
        // Kafka 브로커 주소
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // Consumer Group ID (같은 그룹이면 offset 공유)
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "coupon-service-group");
        // Kafka 메시지 key 문자열로 역직렬화
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // Kafka 메시지 value 문자열로 역직렬화
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // 메시지 자동 커밋 방지 (비즈니스 성공 시 수동 commit 가능)
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * KafkaListener Container Factory 설정
     * - 실제 @KafkaListener 메서드가 메시지를 소비하도록 컨테이너를 생성
     * - ConsumerFactory를 기반으로 동작
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        // 위에서 만든 consumerFactory 사용
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        return factory;
    }

}
