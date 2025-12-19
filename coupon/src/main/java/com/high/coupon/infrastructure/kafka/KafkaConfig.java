package com.high.coupon.infrastructure.kafka;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
@EnableKafka
public class KafkaConfig {

    /**
     * Kafka ProducerFactory 설정
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    // KafkaTemplate (실제로 메시지 보내는 것)
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }


    /**
     * Kafka ConsumerFactory 설정
     * - Consumer 인스턴스를 생성하는 역할
     * - KafkaListener가 실제로 메시지를 가져갈 때 필요한 설정들이 포함
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory(){

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
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            KafkaTemplate<String, String> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        // 위에서 만든 consumerFactory 사용
        factory.setConsumerFactory(consumerFactory());

        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

//        // Dead Letter Recoverer
//        // 실패한 메세지는 원본 토픽명.dlt 토픽으로 전송
//        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
//
//        // 지수 백오프 - 3번 재시도 (= 초기 1초 대기, 2배씩 증가, 최대 10초 대기)
//        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
//        backOff.setInitialInterval(1000L);
//        backOff.setMultiplier(2.0);
//        backOff.setMaxInterval(10000L);
//
//        // 에러 핸들러
//        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
//
//        // 재시도 하지 않을 예외 (파싱 오류)
//        errorHandler.addNotRetryableExceptions(JsonProcessingException.class);
//        errorHandler.addNotRetryableExceptions(SerializationException.class);
//
//        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

}
