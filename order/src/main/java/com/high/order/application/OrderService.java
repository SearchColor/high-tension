package com.high.order.application;

import static java.util.stream.Collectors.toList;

import com.high.order.application.dto.external.ProductResponse;
import com.high.order.application.dto.internal.OrderItemCreateInfo;
import com.high.order.application.dto.request.OrderCreateRequest;
import com.high.order.application.dto.request.OrderItemDeliveryStatusChangeRequest;
import com.high.order.application.dto.request.OrderItemStatusChangeRequest;
import com.high.order.application.dto.request.OrderStatusChangeRequest;
import com.high.order.application.dto.request.OrderUpdateRequest;
import com.high.order.application.dto.response.OrderDetailResponse;
import com.high.order.application.dto.response.OrderItemIdResponse;
import com.high.order.application.dto.response.OrderListResponse;
import com.high.order.application.dto.response.OrderResponse;
import com.high.order.application.exception.OrderBadRequestException;
import com.high.order.application.exception.OrderNotFoundException;
import com.high.order.application.service.CouponService;
import com.high.order.application.service.PaymentService;
import com.high.order.application.service.ProductService;
import com.high.order.domain.entity.Order;
import com.high.order.domain.entity.OrderItem;
import com.high.order.domain.exception.OrderItemNotFoundExeption;
import com.high.order.domain.repository.OrderItemRepository;
import com.high.order.domain.repository.OrderRepository;
import com.high.order.domain.vo.OrderItemStatus;
import com.high.order.domain.vo.OrderStatus;
import com.high.order.infrastructure.security.UserPrincipal;
import jakarta.validation.Valid;
import java.math.BigDecimal;
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
    private final ProductService productService;
    private final CouponService couponService;
    private final PaymentService paymentService;

    private static final String ROLE_PREFIX = "ROLE_";

    //쿠폰 임시
    BigDecimal discountRate = new BigDecimal("10");



    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request, UserPrincipal userPrincipal) {

        UUID customerId = userPrincipal.getUserId();

        //TODO: 유저 검증(유저 조회)

        log.info("주문 생성 시작 ");
        List<OrderItemCreateInfo> orderItemCreateInfoList = request.itemList()
            .stream()
            .map( itemDto -> {

                ProductResponse response =
                    productService.getProductById(itemDto.productId()).getBody().data();

                return new OrderItemCreateInfo(
                    response.productId(),
                    UUID.randomUUID(), //producerId 임시 값
                    response.price(),
                    itemDto.quantity()
                );
            }).toList();
        log.info("feignClient 통신성공 ");

        List<OrderItem> itemList =
            orderItemCreateInfoList.stream()
            .map(item -> OrderItem.create(
                item.productId(),
                item.producerId(),
                item.quantity(),
                item.unitPrice()
            )).toList();

        log.info("itemList 담기 성공");

        //TODO: 쿠폰 검증

        Order order = Order.createOrder(
            customerId,
            request.couponId(),
            request.recipient(),
            request.recipientContact(),
            request.deliveryAddress(),
            request.detailAddress(),
            request.requestMessage(),
            itemList,
            discountRate
        );
        log.info("order 담기 성공");

        Order savedOrder = orderRepository.save(order);

        return OrderResponse.from(savedOrder);
    }



    public OrderDetailResponse getOrderDetail(UUID orderId,  UserPrincipal userPrincipal) {
        /**
         * TODO:
         *   1. 권한에 따른 조회 분기
         *      ㄱ) master - delete된 데이터도 조회 가능
         *      ㄴ) seller - orderItem의 producerID가 본인인 데이터 조회 가능 (삭제된 데이터까지 조회가 가능하게)
         *      ㄷ) user - 자신의 주문만 조회 가능
         */
        UUID userId = userPrincipal.getUserId();
        String userRole = userPrincipal.getRole();

        if(userRole.equals(ROLE_PREFIX + "MASTER")) {
            Order order = getOrderForAdmin(orderId);
            return OrderDetailResponse.from(order);
        }


        if(userRole.equals(ROLE_PREFIX + "SELLER")) {
            Order order = getOrderForAdmin(orderId);

            List<OrderItem> itemList = order.getOrderItems().stream()
                .filter(item -> item.getProducerId().equals(userId))
                .toList();
            return OrderDetailResponse.seller(order, itemList);
        }

        Order order = getOrderForUser(orderId, userId);
        return OrderDetailResponse.from(order);


    }

    public List<OrderListResponse> getOrders(UserPrincipal userPrincipal) {
        /**
         * TODO: 권한에 따른 조회 데이터 필터링
         */
        UUID userId = userPrincipal.getUserId();
        String userRole = userPrincipal.getRole();

        if(userRole.equals(ROLE_PREFIX + "MASTER")) {
            List<Order> orderList = orderRepository.findAll(); //TODO: 페이징
            return orderList.stream().map(OrderListResponse::from).collect(toList());
        }

        if(userRole.equals(ROLE_PREFIX + "SELLER")) { //TODO: product 통신 후 테스트 필요
            List<Order> orderList = orderRepository.findOrdersForSeller(userId);
            return orderList.stream().map(OrderListResponse::from).collect(toList());

        }

        //유저일 때
        List<Order> orderList = orderRepository.findAllByCustomerIdAndDeletedAtIsNull(userId);
        return orderList.stream().map(OrderListResponse::from).collect(toList());
    }


    @Transactional
    public void deleteOrder(UUID orderId, UserPrincipal userPrincipal) {

        UUID userId = userPrincipal.getUserId();
        String userRole = userPrincipal.getRole();
        Order order;

        if(userRole.equals(ROLE_PREFIX + "MASTER")) {
            order = getOrderForAdmin(orderId);

        }

        order = getOrderForUser(orderId, userId);


        if(order.isDeleted()) {
            throw new OrderNotFoundException();
        }
        order.softDelete(userId);
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId, UserPrincipal userPrincipal) {
        UUID customerId = userPrincipal.getUserId();
        String userRole = userPrincipal.getRole();
        Order order;

        order = getOrderForAdmin(orderId);


        if(userRole.equals(ROLE_PREFIX + "USER")) {
            order = getOrderForUser(orderId, customerId);

        }
        //TODO: 결제가 PENDING 상태인지 확인하기 - 수정필요
        //paymentService.getPayment(orderId);


        if (!order.getOrderStatus().canTransitionTo(OrderStatus.CANCELED)) {
            throw new OrderBadRequestException();
        }

        if (order.getOrderStatus().equals(OrderStatus.CREATED)) {
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

    public OrderItemIdResponse cancelOrderItem(UUID orderId, UUID orderItemId,  UserPrincipal userPrincipal) {
        UUID userId = userPrincipal.getUserId();
        String userRole = userPrincipal.getRole();
        Order order;
        OrderItem orderItem;

            order = getOrderForAdmin(orderId);
            orderItem = orderItemRepository.findByOrderItemIdAndDeletedAtIsNull(orderItemId)
                .orElseThrow(OrderItemNotFoundExeption::new);


        if(userRole.equals(ROLE_PREFIX + "SELLER")) {
            order = orderRepository.findOrderForSeller(orderId, userId)
                .orElseThrow(OrderNotFoundException::new);
            orderItem = orderItemRepository.findByOrderIdAndOrderItemIdAndProducerIdAndDeletedAtIsNull(orderId, orderItemId, userId)
                .orElseThrow(OrderItemNotFoundExeption::new);
        }

        if(userRole.equals(ROLE_PREFIX + "USER")) {
            log.info("주문 부분취소 - user 주문 조회");
            order = getOrderForUser(orderId, userId);
            orderItem = orderItemRepository.findByOrderIdAndOrderItemIdAndDeletedAtIsNull(orderId,
                orderItemId).orElseThrow(
                OrderItemNotFoundExeption::new);
            log.info("아이템 존재x");

        }
        //TODO: 결제가 PENDING 상태인지 확인하기

        //전체 주문이 이미 결제된 상태이면 부분 아이템 주문취소 불가능 (미안해요...개발자 실력이 허접이라..ㅜㅜ)
        if(!order.getOrderStatus().equals(OrderStatus.CREATED)) {
            log.info("주문 상태가 \"주문 생성\" 상태가 아님");
            throw new OrderBadRequestException();
        }

        //주문 아이템의 상태가 주문 생성(결제 전) 상태가 아니면 주문취소 불가능
        if(!orderItem.getOrderItemStatus().equals(OrderItemStatus.CREATED)) {
            log.info("주문 상품의 상태가 \"주문 생성\" 상태가 아님");
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

    public OrderResponse changeOrderStatus(UUID orderId, OrderStatusChangeRequest request, UserPrincipal userPrincipal) {
        /**
         * TODO:
         *  1. 주문 존재여부 검증
         *  2. 변경 권한이 있는지 검증
         */

        UUID userId = userPrincipal.getUserId();
        String userRole = userPrincipal.getRole();

        Order order= getOrderForAdmin(orderId);

        OrderStatus nextStatus = request.orderStatus();

        //order = getOrderForUser(orderId, userId);

        order.updateStatus(nextStatus);
        orderRepository.save(order);
        return OrderResponse.from(order);
    }

    //TODO 주문 완료처리 서비스 만들기(주문 생성 -> 주문 완료)

    public OrderItemIdResponse changeOrderItemStatus(UUID orderId, UUID orderItemId, OrderItemStatusChangeRequest request,  UserPrincipal userPrincipal) {

        //TODO: return_request -> return은 판매자,마스터만 변경가능
        OrderItem orderItem = getOrderItemForUser(orderItemId);
        OrderItemStatus currentStatus = getOrderItemForUser(orderItemId).getOrderItemStatus();
        OrderItemStatus nextStatus = request.orderItemStatus();


        if (!currentStatus.canChangeStatus()) {
            log.info("아이템 상태 변경 불가 상태 (SUCCESS or RETURN_REQUEST 상태가 아님)");
            throw new OrderBadRequestException();
        }

        if(!currentStatus.canTransitionTo(nextStatus)) {
            log.info("변경 불가능한 상태로 상태변경 요청이 들어옴");
            throw new OrderBadRequestException();
        }

        if(nextStatus.isCanceled()) {
            //결제 완료 후에 부분취소 불가능(전체취소만 가능)
            log.info("부분취소 요청이 들어옴");
            throw new OrderBadRequestException();
        }

        if (currentStatus.isSuccess() && nextStatus.isReturnRequest()) {
            //TODO: 결제완료 상태인지 확인 (feignClient)
            //TODO: 결제에 환불요청 보내기 (Kafka - 오케스트레이션에서 )
            orderItem.updateItemStatus(OrderItemStatus.RETURN_REQUEST);
        }

        if (currentStatus.isReturnRequest() && nextStatus.isReturned()) {
            //TODO: 결제가 환불완료 상태인지 확인 -> 환불상태가 아니면 exception
            //TODO 배송의 상태가 배송준비중이거나 배송중이면 안됨
            orderItem.updateItemStatus(OrderItemStatus.RETURNED);

        }

        orderItemRepository.save(orderItem);
        return OrderItemIdResponse.from(orderItem);

    }


    public OrderItemIdResponse changeOrderItemDeliveryStatus(UUID orderId, UUID orderItemId, OrderItemDeliveryStatusChangeRequest request,  UserPrincipal userPrincipal) {
        OrderItem orderItem = getOrderItemForUser(orderItemId);

        if(orderItem.getOrderItemStatus().cannotChangeDeliveryStatus()) {
            log.info("주문이 CREATED 상태이거나 CANCELED면 배송상태 변경 불가");
            throw new OrderBadRequestException();
        }
        orderItem.updateDeliveryStatus(request.deliveryStatus());
        orderItemRepository.save(orderItem);

        return OrderItemIdResponse.from(orderItem);
    }



    public OrderResponse updateOrder(UUID orderId, @Valid OrderUpdateRequest request, UserPrincipal userPrincipal) {

        UUID customerId = userPrincipal.getUserId();
        String userRole = userPrincipal.getRole();

        if(userRole.equals(ROLE_PREFIX + "MASTER")) {

        }

        if(userRole.equals(ROLE_PREFIX + "SELLER")) {

        }


        Order order = getOrderForUser(orderId, customerId);




        if(!order.getOrderStatus().isUpdatableDeliveryInfo()) {
            log.info("주문이 취소되어 배송정보 변경이 불가능합니다.");
            throw new OrderBadRequestException();
        }

        if(!order.validateUpdatableDeliveryInfo()) {
            log.info("배송이 시작되어 배송정보 변경이 불가능합니다.");
            throw new OrderBadRequestException();
        }

        String recipient = request.recipient().orElse(null);
        String recipientContact =  request.recipientContact().orElse(null);
        String deliveryAddress = request.deliveryAddress().orElse(null);
        String detailAddress = request.detailAddress().orElse(null);
        String requestMessage = request.requestMessage().orElse(null);

        if(recipientContact != null && !recipientContact.matches("^(010)(-?\\d{4})(-?\\d{4})$")) {
            throw new OrderBadRequestException();
        }

        order.updateDeliveryInfo(recipient, recipientContact, deliveryAddress, detailAddress, requestMessage);
        orderRepository.save(order);

        return OrderResponse.from(order);
    }


    public Order getOrderForUser(UUID orderId, UUID customerId) {
        return orderRepository.findByOrderIdAndCustomerIdAndDeletedAtIsNull(orderId, customerId).orElseThrow(OrderNotFoundException::new);
    }

    public Order getOrderForAdmin(UUID orderId) {
        return orderRepository.findById(orderId).orElseThrow(OrderNotFoundException::new);
    }

    public OrderItem getOrderItemForUser(UUID orderItemId) {
        return orderItemRepository.findByOrderItemIdAndDeletedAtIsNull(orderItemId).orElseThrow(OrderItemNotFoundExeption::new);
    }
}
