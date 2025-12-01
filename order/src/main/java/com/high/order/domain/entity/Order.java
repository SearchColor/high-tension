package com.high.order.domain.entity;

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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="p_order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderId;

    private UUID customerId;

    private UUID couponId;

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

    private Order(UUID customerId, UUID couponId,
        String recipient, String recipientContact,
        String deliveryAddress, String detailAddress,
        String requestMessage) {
        this.customerId = customerId;
        this.couponId = couponId;
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

    /**
     * 단일 상품 주문 생성
     */
    public static Order createOrder (
        UUID customerId,
        UUID couponId,
        String recipient,
        String recipientContact,
        String deliveryAddress,
        String detailAddress,
        String requestMessage,
        List<OrderItem> orderItems
) {

        Order order = new Order(
            customerId,
            couponId,
            recipient,
            recipientContact,
            deliveryAddress,
            detailAddress,
            requestMessage
        );

        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }

        order.calculateAmounts();
        return order;
    }



    private void addOrderItem(OrderItem orderItem) {
        if (orderItem == null) {
            throw new IllegalArgumentException("주문 아이템은 null일 수 없습니다.");
        }
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    private void calculateAmounts() {
        this.totalPrice = orderItems.stream()
            .mapToInt(OrderItem::getItemTotalPrice)
            .sum();

        this.discountAmount = calculateDiscount();
        this.paidAmount = this.totalPrice - this.discountAmount;
    }

    private Integer calculateDiscount() {
        // TODO: 쿠폰 할인율 어떻게?
        return couponId != null ? 0 : 0;
    }
}
