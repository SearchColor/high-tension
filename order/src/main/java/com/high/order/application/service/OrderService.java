package com.high.order.application.service;

import com.high.order.application.dto.internal.OrderItemCreateInfo;
import com.high.order.application.dto.request.OrderCreateRequest;
import com.high.order.application.dto.response.OrderCreateResponse;
import com.high.order.application.dto.response.OrderDetailResponse;
import com.high.order.application.exception.OrderNotFoundException;
import com.high.order.domain.entity.Order;
import com.high.order.domain.entity.OrderItem;
import com.high.order.domain.repository.OrderRepository;
import com.high.order.infrastructure.client.ProductDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    //FeignClient 통신 전 임시데이터
    UUID customerId =  UUID.randomUUID(); //유저
    //UUID producerId = UUID.randomUUID(); //product
    //Integer unitPrice = 1000; //product



    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request) {

        /**
         * TODO:
         *  1. 로그인한 사용자 권한 검증
         *  2. productID 존재여부 검증
         *  3. couponID 존재여부 검증
         */

        List<OrderItemCreateInfo> orderItemCreateInfoList = request.itemList()
            .stream()
            .map( itemDto -> {
                //feignClient 구현 후 mapper 사용예정
                ProductDto productDto = ProductDto.init();
                return new OrderItemCreateInfo(
                    itemDto.productId(),
                    productDto.producerId(),
                    productDto.price(),
                    itemDto.quantity()
                );
            }).toList();

        List<OrderItem> itemList = orderItemCreateInfoList.stream()
            .map(item -> OrderItem.create(
                item.productId(),
                item.producerId(),
                item.quantity(),
                item.unitPrice()
            )).toList();

        Order order = Order.createOrder(
            customerId,
            request.couponId(),
            request.recipient(),
            request.recipientContact(),
            request.deliveryAddress(),
            request.detailAddress(),
            request.requestMessage(),
            itemList
        );
        Order savedOrder = orderRepository.save(order);

        return OrderCreateResponse.from(savedOrder);
    }



    public OrderDetailResponse getOrderDetail(UUID orderId) {
        /**
         * TODO:
         *   1. 권한에 따른 조회 분기
         *      ㄱ) master - delete된 데이터도 조회 가능
         *      ㄴ) seller - orderItem의 producerID가 본인인 데이터 조회 가능 (삭제된 데이터까지 조회가 가능하게)
         *      ㄷ) user - 자신의 주문만 조회 가능
         */
        Order order = orderRepository.findById(orderId).orElseThrow(OrderNotFoundException::new);
        return OrderDetailResponse.from(order);

    }

    @Transactional
    public void deleteOrder(UUID orderId) {
        //TODO: 삭제 권한 확인
        Order order = orderRepository.findById(orderId).orElseThrow(OrderNotFoundException::new);

        if(order.isDeleted()) {
            throw new OrderNotFoundException();
        }
        //삭제자 임시
        order.softDelete(UUID.randomUUID().toString());
    }
}
