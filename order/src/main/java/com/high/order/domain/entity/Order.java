package com.high.order.domain.entity;

import com.high.order.domain.exception.DeliveryStatusChangeNotAllowedException;
import com.high.order.domain.exception.OrderCancellationNotAllowedByItemStatusException;
import com.high.order.domain.exception.OrderItemNotFoundInOrderException;
import com.high.order.domain.exception.OrderStatusChangeNotAllowedException;
import com.high.order.domain.vo.OrderItemStatus;
import com.high.order.domain.vo.OrderStatus;
import com.library.jpa.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Table(name="p_order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderId;

    private UUID customerId;

    private UUID couponIssueId;

    private Integer totalPrice;

    private Integer discountAmount;

    private Integer paidAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private String recipient;

    private String recipientContact;

    private String deliveryAddress;

    private String detailAddress;

    private String requestMessage;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    private Order(UUID customerId, UUID couponIssueId,
        String recipient, String recipientContact,
        String deliveryAddress, String detailAddress,
        String requestMessage) {
        this.customerId = customerId;
        this.couponIssueId = couponIssueId;
        this.orderStatus = OrderStatus.CREATED;
        this.recipient = recipient;
        this.recipientContact = recipientContact;
        this.deliveryAddress = deliveryAddress;
        this.detailAddress = detailAddress;
        this.requestMessage = requestMessage;
        this.totalPrice = 0;
        this.discountAmount = 0;
        this.paidAmount = 0;
    }


    public static Order createOrder (
        UUID customerId,
        UUID couponIssueId,
        String recipient,
        String recipientContact,
        String deliveryAddress,
        String detailAddress,
        String requestMessage,
        List<OrderItem> orderItems,
        BigDecimal discountPercent
) {

        Order order = new Order(
            customerId,
            couponIssueId,
            recipient,
            recipientContact,
            deliveryAddress,
            detailAddress,
            requestMessage
        );

        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }

        order.calculateTotalAmounts(discountPercent);
        return order;
    }



    private void addOrderItem(OrderItem orderItem) {
        if (orderItem == null) {
            throw new OrderItemNotFoundInOrderException();
        }
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    private void calculateTotalAmounts(BigDecimal discountAmount) {
        this.totalPrice = orderItems.stream()
            .mapToInt(OrderItem::getItemTotalPrice)
            .sum();

        this.discountAmount = calculateDiscount(discountAmount);
        this.paidAmount = this.totalPrice - this.discountAmount;
    }

    //V2에서 deprecated
    public void updateTotalPrice(Integer recalculatedPrice) {
        this.totalPrice = recalculatedPrice;
    }

    private Integer calculateDiscount(BigDecimal discountPercent) {
        if ( couponIssueId != null ) {
            BigDecimal price = BigDecimal.valueOf(totalPrice);
            BigDecimal discountRate = discountPercent.divide(new BigDecimal("100"), 4,
                RoundingMode.HALF_UP);
            BigDecimal discountAmountBd = price.multiply(discountRate);

            return discountAmountBd.setScale(0, RoundingMode.HALF_UP).intValue();

        }
        return 0;
    }

    public void updateStatus(OrderStatus nextStatus) {
        if(!this.orderStatus.canTransitionTo(nextStatus)) {
            throw new OrderStatusChangeNotAllowedException();
        }
        this.orderStatus = nextStatus;

        OrderItemStatus nextItemStatus = switch (nextStatus) {
            case CREATED -> OrderItemStatus.CREATED;
            case SUCCESS -> OrderItemStatus.SUCCESS;
            case CANCELED -> OrderItemStatus.CANCELED;
        };

        for(OrderItem orderItem : orderItems) {
            orderItem.updateItemStatus(nextItemStatus);
        }
    }


    public void validateUpdatableDeliveryInfo() {
        if(!orderItems.stream().allMatch(item -> item.getDeliveryStatus().isUpdatableDeliveryInfo())) {
            log.error("배송이 시작되어 배송정보 변경이 불가능");
            throw new DeliveryStatusChangeNotAllowedException();
        }
    }

    public void updateDeliveryInfo( String recipient,
                                    String recipientContact,
                                    String deliveryAddress,
                                    String detailAddress,
                                    String requestMessage) {
        if(recipient != null) this.recipient = recipient;
        if(recipientContact != null) this.recipientContact = recipientContact;
        if(deliveryAddress != null) this.deliveryAddress = deliveryAddress;
        if(detailAddress != null) this.detailAddress = detailAddress;
        if(requestMessage != null) this.requestMessage = requestMessage;
    }

    // 전체 취소
    public void cancelOrder() {

        for (OrderItem item : this.orderItems) {
            item.cancel();
        }
        this.orderStatus = OrderStatus.CANCELED;
    }

    public void validateCancellableForOrderItems() {
        if(!this.getOrderItems().stream().allMatch(OrderItem::isCancellable)) {
            throw new OrderCancellationNotAllowedByItemStatusException();
        }
    }

    // 단일 아이템 취소
    public void cancelItem(UUID orderItemId) {
        if (! (this.orderStatus.isCreated() || this.orderStatus.isSuccess()) ) {
            throw new OrderCancellationNotAllowedByItemStatusException();
        }

        OrderItem item = this.orderItems.stream()
            .filter(oi -> oi.getOrderItemId().equals(orderItemId))
            .findFirst()
            .orElseThrow(OrderItemNotFoundInOrderException::new);

        item.getOrderItemStatus().validatePartialCancellationForOrderItem();

        item.cancel();
    }

//    public void reCalculateTotalPrice(OrderItem orderItem, BigDecimal discountPercent) {
//        this.totalPrice -= orderItem.getItemTotalPrice();
//        log.info("취소 후 최종 금액  : {}", totalPrice);
//    }
//
//    public void reCalculatePaidAmount(OrderItem orderItem, BigDecimal discountPercent
//    ) {
//        this.paidAmount -= orderItem.calculateCancelAmounts(discountPercent);
//        log.info("취소 후 결제 금액  : {}", paidAmount);
//        this.discountAmount = calculateDiscount(discountPercent);
//
//    }

    //부분 취소 후 취소된 부분만 금액에서 차감하지 않고 확실하게 주문 금액을 새로 재계산(아이템이 많을 시 성능 문제는 있을듯..)
    public void recalculateAllAmounts(BigDecimal discountPercent) {
        //1. 총액 재계산 (취소되지 않은 아이템만)
        this.totalPrice = orderItems.stream()
            .filter(item -> item.getOrderItemStatus() != OrderItemStatus.CANCELED)
            .mapToInt(OrderItem::getItemTotalPrice)
            .sum();

        //2. 할인액 재계산
        this.discountAmount = calculateDiscount(discountPercent);

        //3. 실제 결제액 재계산
        this.paidAmount = this.totalPrice - this.discountAmount;

        log.info("재계산 완료 - 총액: {}, 할인: {}, 결제: {}", totalPrice, discountAmount, paidAmount);
    }

    @Override
    public void softDelete(UUID deletedBy) {
        super.softDelete(deletedBy);
        this.orderItems.forEach(orderItem -> orderItem.softDelete(deletedBy));
    }
}
