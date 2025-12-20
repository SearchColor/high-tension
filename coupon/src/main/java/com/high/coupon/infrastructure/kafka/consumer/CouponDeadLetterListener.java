//package com.high.coupon.infrastructure.kafka.consumer;
// todo: 비교를 위한 백업
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.support.Acknowledgment;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//public class CouponDeadLetterListener {
//
//    /**
//     * 쿠폰 발급 실패 처리
//     * - 원본: coupon-issue-request
//     * - DLT 토픽: coupon-issue-request.DLT
//     */
//    @KafkaListener(
//            topics = "coupon-issue-request-dlt",
//            groupId = "coupon-issue-dlt-group",
//            containerFactory = "kafkaListenerContainerFactory")
//    public void consumeIssueFail(String message, Acknowledgment ack){
//
//        log.error("🚨[COUPON ISSUE DLT 발생] 쿠폰 발급 실패 메세지: {}", message);
//
//        // 이 과정에 알림 전송, 에러 로그 DB 저장 등을 추가해 확장 할 수 있음
//
//        ack.acknowledge();
//    }
//
//    /**
//     * 쿠폰 사용 실패 처리
//     * - 원본: coupon-user-request
//     * - DLT 토픽: coupon-use-request.DLT
//     */
//    @KafkaListener(
//            topics = "coupon-use-request-dlt",
//            groupId = "coupon-use-dlt-group",
//            containerFactory = "kafkaListenerContainerFactory")
//    public void consumeUseFail(String message, Acknowledgment ack) {
//
//        log.error("🚨 [COUPON USE DLT 발생] 쿠폰 사용 실패 메시지: {}", message);
//
//        ack.acknowledge();
//    }
//}
