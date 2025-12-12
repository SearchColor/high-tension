package com.high.order.application;

import static java.util.stream.Collectors.toList;

import com.high.order.application.dto.external.CouponResponse;
import com.high.order.application.dto.external.PaymentResponse;
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
import com.high.order.application.exception.NoPermissionToChangeOrderItemStatusException;
import com.high.order.application.exception.OrderBadRequestException;
import com.high.order.application.exception.OrderItemStatusNotAllowedException;
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


    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request, UUID userId, String userRole) {

        //TODO: 유저 검증(유저 조회)
        try {
            log.info("주문 생성 시작 ");
            List<OrderItemCreateInfo> orderItemCreateInfoList = request.itemList()
                .stream()
                .map(itemDto -> {

                    ProductResponse productResponse = getProduct(itemDto.productId());
                    log.info("[OrderService] createOrder - feignClient product 조회 - 판매자 ID: {}", productResponse.seller());
                    return new OrderItemCreateInfo(
                        productResponse.id(),
                        productResponse.seller(),
                        productResponse.price(),
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

            CouponResponse couponResponse = null;

            if (request.couponId() != null) {
                couponResponse = getCoupon(request.couponId());
                log.info("[OrderService] createOrder - feignClient coupon 조회 - couponId: {}",
                    couponResponse.couponIssueId());
            }

            BigDecimal discountRate = request.couponId() == null? null: couponResponse.discountRate();

            Order order = Order.createOrder(
                userId,
                request.couponId(),
                request.recipient(),
                request.recipientContact(),
                request.deliveryAddress(),
                request.detailAddress(),
                request.requestMessage(),
                itemList,
                discountRate
            );

            Order savedOrder = orderRepository.save(order);

            return OrderResponse.from(savedOrder);

        } catch (Exception e) {
            throw e;
        }
    }



    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(UUID orderId, UUID userId, String userRole) {

        if(userRole.equals("MASTER")) {
            Order order = getOrderForAdmin(orderId);
            return OrderDetailResponse.from(order);
        }

        if(userRole.equals("SELLER")) {
            Order order = getOrderForAdmin(orderId);

            List<OrderItem> itemList = order.getOrderItems().stream()
                .filter(item -> item.getProducerId().equals(userId))
                .toList();
            return OrderDetailResponse.seller(order, itemList);
        }

        Order order = getOrderForUser(orderId, userId);

        return OrderDetailResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderListResponse> getOrders(UUID userId, String userRole) {
        List<Order> orderList;

        if(userRole.equals("MASTER")) {
            orderList = orderRepository.findAll(); //TODO: 페이징
            return orderList.stream().map(OrderListResponse::from).collect(toList());
        }

        if(userRole.equals("SELLER")) {
            orderList = orderRepository.findOrdersForSeller(userId);
            return orderList.stream().map(OrderListResponse::from).collect(toList());
        }

        orderList = orderRepository.findAllByCustomerIdAndDeletedAtIsNull(userId);
        return orderList.stream().map(OrderListResponse::from).collect(toList());
    }


    @Transactional
    public void deleteOrder(UUID orderId, UUID userId, String userRole) {
        Order order = null;

        if(userRole.equals("MASTER")) {
            order = getOrderForAdmin(orderId);
        }

        if(userRole.equals("USER")) {
            order = getOrderForUser(orderId, userId);
        }

        if(order.isDeleted()) {
            throw new OrderNotFoundException();
        }

        order.softDelete(userId);
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId, UUID userId, String userRole) {

        Order order = null;

        if(userRole.equals("MASTER")) {
            order = getOrderForAdmin(orderId);
        }

        if(userRole.equals("USER")) {
            order = getOrderForUser(orderId, userId);

        }

        //결제 검증
//        PaymentResponse paymentResponse = null;
//        try {
//            paymentResponse = getPayment(orderId);
//            log.info("payment 통신 성공 - 결제 ID : {}, 주문 ID : {}, 결제 status : {}" ,
//                paymentResponse.paymentId(), paymentResponse.orderId(), paymentResponse.status());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            e.getMessage();
//            throw e;
//        }
//
//        if(paymentResponse == null) {
//            throw new PaymentNotFoundException();
//        }
//        if (!(paymentResponse.status().equals("REQUESTED") || paymentResponse.status().equals("COMPLETED"))) {
//            throw new NoPermissionToCancelOrderException();
//        }

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
           if(!order.getOrderItems().stream().allMatch(OrderItem::isCancellable)) {
               throw new OrderBadRequestException();
           }
               order.cancelOrder();
               orderRepository.save(order);
               //TODO: 재고 복원, 쿠폰 사용 되돌리기, 결제 취소 요청
               return OrderResponse.from(order);

    }

    @Transactional
    public OrderItemIdResponse cancelOrderItem(UUID orderId, UUID orderItemId, UUID userId, String userRole) {

        Order order = null;
        OrderItem orderItem = null;

        if(userRole.equals("MASTER")) {
            order = getOrderForAdmin(orderId);
            orderItem = getActiveOrderItemForAdmin(orderItemId);

        }
        if(userRole.equals("SELLER")) {
            order = orderRepository.findOrderForSeller(orderId, userId)
                .orElseThrow(OrderNotFoundException::new);
            orderItem = getActiveOrderItemForSeller(orderId, orderItemId, userId);
        }

        if(userRole.equals("USER")) {
            order = getOrderForUser(orderId, userId);
            orderItem = getOrderItemForUser(orderItemId, userId);

        }

        //결제 검증
//        PaymentResponse paymentResponse = null;
//        try {
//            paymentResponse = getPayment(orderId);
//            log.info("payment 통신 성공 - 결제 ID : {}, 주문 ID : {}, 결제 status : {}" ,
//                paymentResponse.paymentId(), paymentResponse.orderId(), paymentResponse.status());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            e.getMessage();
//            throw e;
//        }
//
//        if(paymentResponse == null) {
//            throw new PaymentNotFoundException();
//        }
//
//        if (!paymentResponse.status().equals("REQUESTED")) {
//            throw new NoPermissionToCancelOrderItemException();
//        }

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

    @Transactional
    public OrderResponse changeOrderStatus(UUID orderId, OrderStatusChangeRequest request, UUID userId, String userRole) {

        Order order= getOrderForAdmin(orderId);

        OrderStatus nextStatus = request.orderStatus();

        order.updateStatus(nextStatus);
        orderRepository.save(order);
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderItemIdResponse changeOrderItemStatusForRefund(UUID orderId, UUID orderItemId, OrderItemStatusChangeRequest request, UUID userId, String userRole) {

        OrderItem orderItem = null;
        if(userRole.equals("SELLER")) {
            orderItem = getActiveOrderItemForSeller(orderId, orderItemId, userId);
        }

        if(userRole.equals("MASTER")) {
            orderItem = getActiveOrderItemForAdmin(orderItemId);
        }

        if(userRole.equals("USER")) {
            orderItem = getOrderItemForUser(orderItemId, userId);
        }

        OrderItemStatus currentStatus = orderItem.getOrderItemStatus(); //현재 상태
        OrderItemStatus nextStatus = request.orderItemStatus(); //변경할 상태


        if (!currentStatus.canChangeStatus()) {
            log.info("아이템 상태 변경 불가 상태 (SUCCESS or RETURN_REQUEST 상태가 아님)");
            throw new OrderItemStatusNotAllowedException();
        }

        if (userRole.equals("USER") && nextStatus.equals(OrderItemStatus.RETURNED)) { //USER는 환불완료 상태로 변경 불가
            throw new NoPermissionToChangeOrderItemStatusException();
        }

        if(!currentStatus.canTransitionTo(nextStatus)) { //상태 변경 정책에 위배되지 않는지 검증
            log.info("변경 불가능한 상태로 상태변경 요청이 들어옴");
            throw new OrderItemStatusNotAllowedException();
        }

        if(nextStatus.isCanceled()) {
            //결제 완료 후에 부분취소 불가능(전체취소만 가능)
            log.info("부분취소 요청이 들어옴");
            throw new OrderItemStatusNotAllowedException();
        }

        //결제 내역 조회
//        PaymentResponse paymentResponse = null;
//        try {
//            paymentResponse = getPayment(orderId);
//            log.info("payment 통신 성공 - 결제 ID : {}, 주문 ID : {}, 결제 status : {}" ,
//                paymentResponse.paymentId(), paymentResponse.orderId(), paymentResponse.status());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            e.getMessage();
//            throw e;
//        }
//
//        if(paymentResponse == null) {
//            throw new PaymentNotFoundException();
//        }
//
//        if (!(paymentResponse.status().equals("COMPLETED") || paymentResponse.status().equals("REFUNDED"))) {
//            throw new NoPermissionToCancelOrderException();
//        }

        //결제 완료 상태이면 환불 요청 가능
        if (nextStatus.isReturnRequest() /* && paymentResponse.status().equals("COMPLETED")*/) {
            //TODO: 결제에 환불요청 보내기 (Kafka - 오케스트레이션에서 )
            orderItem.updateItemStatus(OrderItemStatus.RETURN_REQUEST);
        }

        //배송완료 상태이고, 환불 요청상태이면 환불완료 상태로 변경 가능
        if (nextStatus.isReturned() && orderItem.getDeliveryStatus().canTransitionToRefund() /* && paymentResponse.status().equals("REFUNDED") */ ) {
            orderItem.updateItemStatus(OrderItemStatus.RETURNED);

        }

        orderItemRepository.save(orderItem);
        return OrderItemIdResponse.from(orderItem);

    }

    @Transactional
    public OrderItemIdResponse changeOrderItemDeliveryStatus(UUID orderId, UUID orderItemId, OrderItemDeliveryStatusChangeRequest request, UUID userId, String userRole) {

        OrderItem orderItem = null;
        if(userRole.equals("SELLER")) {
            orderItem = getActiveOrderItemForSeller(orderId, orderItemId, userId);
        }


        if(userRole.equals("MASTER")) {
            orderItem = getActiveOrderItemForAdmin(orderItemId);
        }


        if(orderItem.getOrderItemStatus().cannotChangeDeliveryStatus()) {
            log.info("주문이 CREATED 상태이거나 CANCELED면 배송상태 변경 불가");
            throw new OrderBadRequestException();
        }
        orderItem.updateDeliveryStatus(request.deliveryStatus());
        orderItemRepository.save(orderItem);

        return OrderItemIdResponse.from(orderItem);
    }


    @Transactional
    public OrderResponse updateOrder(UUID orderId, @Valid OrderUpdateRequest request,  UUID userId, String userRole) {

        Order order = null;

        if(userRole.equals("USER")) {
            order = getOrderForUser(orderId, userId);
        }

        if(userRole.equals("MASTER")) {
            order = getOrderForAdmin(orderId);
        }

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

        //recipientContact가 null일 수 있으므로 null이 아닐 경우에 한하여 서비스에서 형식 검증
        if(recipientContact != null && !recipientContact.matches("^(010)(-?\\d{4})(-?\\d{4})$")) {
            throw new OrderBadRequestException();
        }

        order.updateDeliveryInfo(recipient, recipientContact, deliveryAddress, detailAddress, requestMessage);
        orderRepository.save(order);

        return OrderResponse.from(order);
    }

    @Transactional
    public void successOrder(UUID orderId, UUID userId) {
        Order order = getOrderForUser(orderId, userId);
        order.updateStatus(OrderStatus.SUCCESS);
        orderRepository.save(order);
    }

    public Order getOrderForUser(UUID orderId, UUID customerId) {
        return orderRepository.findByOrderIdAndCustomerIdAndDeletedAtIsNull(orderId, customerId).orElseThrow(OrderNotFoundException::new);
    }

    public Order getOrderForAdmin(UUID orderId) {
        return orderRepository.findById(orderId).orElseThrow(OrderNotFoundException::new);
    }

    public OrderItem getActiveOrderItemForAdmin(UUID orderItemId) {
        return orderItemRepository.findByOrderItemIdAndDeletedAtIsNull(orderItemId).orElseThrow(OrderItemNotFoundExeption::new);
    }

    public OrderItem getActiveOrderItemForSeller(UUID orderId, UUID orderItemId, UUID producerId) {
        return orderItemRepository.findByOrderIdAndOrderItemIdAndProducerIdAndDeletedAtIsNull(orderId, orderItemId, producerId).orElseThrow(OrderItemNotFoundExeption::new);
    }

    public OrderItem getOrderItemForUser(UUID orderItemId, UUID userId) {
        return orderItemRepository.findOrderItemForUser(orderItemId, userId).orElseThrow(OrderItemNotFoundExeption::new);
    }

    //feignClient 통신 메서드들
    public ProductResponse getProduct (UUID productId) {
        return productService.getProductByOrderId(productId).data();
    }

    public CouponResponse getCoupon(UUID couponIssueId) {
        return couponService.validateCoupon(couponIssueId).data();
    }

    public PaymentResponse getPayment(UUID orderId) {
        return paymentService.getPaymentByOrderId(orderId);
    }
}
