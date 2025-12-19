package com.high.orchestration.infrastructure.kafka.config;


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

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    // 공통 Consumer 설정
    private Map<String, Object> commonConsumerConfigs(String groupId) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return properties;
    }


    @Bean
    public ConsumerFactory<String, String> orchestrationConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
            commonConsumerConfigs("orchestration-consumer-group"));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory
            = new ConcurrentKafkaListenerContainerFactory<>();

        kafkaListenerContainerFactory.setConsumerFactory(orchestrationConsumerFactory());

        return kafkaListenerContainerFactory;
    }



    //DLQ 설정
    @Bean
    public ConsumerFactory<String, String> dlqConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
            commonConsumerConfigs("dlq-consumer-group")
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> dlqKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(dlqConsumerFactory());
        factory.setConcurrency(1); // DLQ는 단일 스레드로 처리

        // DLQ는 재시도 없이 바로 처리
        factory.setCommonErrorHandler(null);

        return factory;
    }
}
