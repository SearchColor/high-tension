package com.high.order.application.service;

import static java.util.stream.Collectors.toList;

import com.high.order.application.dto.internal.OrderItemCreateInfo;
import com.high.order.application.dto.request.OrderCreateRequest;
import com.high.order.application.dto.request.OrderStatusChangeRequest;
import com.high.order.application.dto.response.OrderDetailResponse;
import com.high.order.application.dto.response.OrderItemIdResponse;
import com.high.order.application.dto.response.OrderListResponse;
import com.high.order.application.dto.response.OrderResponse;
import com.high.order.application.exception.OrderBadRequestException;
import com.high.order.domain.exception.OrderItemNotFoundExeption;
import com.high.order.domain.exception.OrderNotFoundException;
import com.high.order.domain.entity.Order;
import com.high.order.domain.entity.OrderItem;
import com.high.order.domain.repository.OrderItemRepository;
import com.high.order.domain.repository.OrderRepository;
import com.high.order.domain.vo.OrderItemStatus;
import com.high.order.domain.vo.OrderStatus;
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
    private final OrderItemRepository orderItemRepository;

    //FeignClient 통신 전 임시데이터
    UUID customerId =  UUID.randomUUID(); //유저
    //UUID producerId = UUID.randomUUID(); //product
    //Integer unitPrice = 1000; //product



    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {

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

        return OrderResponse.from(savedOrder);
    }



    public OrderDetailResponse getOrderDetail(UUID orderId) {
        /**
         * TODO:
         *   1. 권한에 따른 조회 분기
         *      ㄱ) master - delete된 데이터도 조회 가능
         *      ㄴ) seller - orderItem의 producerID가 본인인 데이터 조회 가능 (삭제된 데이터까지 조회가 가능하게)
         *      ㄷ) user - 자신의 주문만 조회 가능
         */
        Order order = orderRepository.findByOrderIdAndDeletedAtIsNull(orderId).orElseThrow(OrderNotFoundException::new);
        return OrderDetailResponse.from(order);

    }

    public List<OrderListResponse> getOrders() {
        /**
         * TODO: 권한에 따른 조회 데이터 필터링
         */
        List<Order> orderList = orderRepository.findAllByDeletedAtIsNull();
        return orderList.stream().map(OrderListResponse::from).collect(toList());
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

    @Transactional
    public OrderResponse cancelOrder(UUID orderId) {

        Order order = orderRepository.findByOrderIdAndDeletedAtIsNull(orderId).orElseThrow(OrderNotFoundException::new);
        //TODO: 결제가 PENDING 상태인지 확인하기
        if (!order.getOrderStatus().canTransitionTo(OrderStatus.CANCELED)) {
            throw new OrderBadRequestException();
        }

        if (order.getOrderStatus().equals(OrderStatus.CREATED)) {
            //TODO: 주문 아이템들이 전부 CREATE 상태여야함


            order.cancelOrder();
            orderRepository.save(order);
            //TODO: 재고 복원, 쿠폰 사용 되돌리기 kafka 요청
            return OrderResponse.from(order);
        }


        //주문 아이템중 하나라도 환불신청 또는 환불 상태일 때 주문취소 불가능
           if(! order.getOrderItems().stream().allMatch(OrderItem::isCancellable)) {
               throw new OrderBadRequestException();
           }
               order.cancelOrder();
               orderRepository.save(order);
               //TODO: 재고 복원, 쿠폰 사용 되돌리기, 결제 취소 요청
               return OrderResponse.from(order);

    }

    public OrderItemIdResponse cancelOrderItem(UUID orderId, UUID orderItemId) {
        Order order = orderRepository.findByOrderIdAndDeletedAtIsNull(orderId).orElseThrow(OrderNotFoundException::new);
        OrderItem orderItem = orderItemRepository.findByOrderItemIdAndDeletedAtIsNull(orderItemId).orElseThrow(
            OrderItemNotFoundExeption::new);
        //TODO: 결제가 PENDING 상태인지 확인하기

        //전체 주문이 이미 결제된 상태이면 부분 아이템 주문취소 불가능 (미안해요...개발자 실력이 허접이라..ㅜㅜ)
        if(!order.getOrderStatus().equals(OrderStatus.CREATED)) {
            throw new OrderBadRequestException();
        }

        //주문 아이템의 상태가 주문 생성(결제 전) 상태가 아니면 주문취소 불가능
        if(!orderItem.getOrderItemStatus().equals(OrderItemStatus.CREATED)) {
            throw new OrderBadRequestException();
        }

        order.cancelItem(orderItemId);
        orderRepository.save(order);

        //TODO: 쿠폰적용 금액으로 다시 로직 작성해야함
        Integer recalculatingPrice =  order.getTotalPrice() - orderItem.getItemTotalPrice();

        order.updateTotalPrice(recalculatingPrice);
        orderRepository.save(order);
        //TODO: 취소한 상품 재고 복원 요청
        return OrderItemIdResponse.from(orderItem);
    }

    public OrderResponse changeOrderStatus(UUID orderId, OrderStatusChangeRequest request) {
        /**
         * TODO:
         *  1. 주문 존재여부 검증
         *  2. 변경 권한이 있는지 검증
         */
        OrderStatus nextStatus = request.orderStatus();
        Order order = orderRepository.findByOrderIdAndDeletedAtIsNull(orderId).orElseThrow(OrderNotFoundException::new);
        order.updateStatus(nextStatus);
        orderRepository.save(order);
        return OrderResponse.from(order);
    }
}
