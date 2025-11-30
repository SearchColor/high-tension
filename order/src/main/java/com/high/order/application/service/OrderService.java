package com.high.order.application.service;

import com.high.order.application.dto.request.ProductOrderCreateRequest;
import com.high.order.application.dto.response.OrderCreateResponse;
import com.high.order.application.dto.response.OrderDetailResponse;
import com.high.order.domain.entity.Order;
import com.high.order.domain.repository.OrderRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    //FeignClient 통신 전 임시데이터
    UUID customerId =  UUID.randomUUID(); //유저
    UUID producerId = UUID.randomUUID(); //product
    Integer unitPrice = 1000; //product



    public OrderCreateResponse createSingleProductOrder(ProductOrderCreateRequest request) {

        /**
         * TODO:
         *  1. 로그인한 사용자 권한 검증
         *  2. productID 존재여부 검증
         *  3. couponID 존재여부 검증
         */
        Order order = Order.createFromSingleProduct(customerId, request, producerId, unitPrice);
        orderRepository.save(order);

        return OrderCreateResponse.from(order);
    }

    public OrderDetailResponse getOrderDetail(UUID orderId) {
        /**
         * TODO:
         *   1. 권한에 따른 조회 분기
         *      ㄱ) master - delete된 데이터도 조회 가능
         *      ㄴ) seller - orderItem의 producerID가 본인인 데이터 조회 가능 (삭제된 데이터까지 조회가 가능하게)
         *      ㄷ) user - 자신의 주문만 조회 가능
         */
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("해당 주문이 존재하지 않습니다."));
        return OrderDetailResponse.from(order);

    }
}
