package com.high.order.domain.entity;

import com.high.order.application.dto.internal.OrderItemCreateInfo;
import com.high.order.application.dto.request.OrderCreateRequest;
import com.high.order.domain.vo.OrderStatus;
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
public class Order {
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
    private List<OrderItem> orderItems;

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
        this.orderItems = new ArrayList<>();
    }

    /**
     * 단일 상품 주문 생성
     */
    public static Order createOrder( UUID customerId, OrderCreateRequest request,
                                     List<OrderItemCreateInfo> orderItemCreateInfoList) {

        Order order = new Order(
            customerId,
            request.couponId(),
            request.recipient(),
            request.recipientContact(),
            request.deliveryAddress(),
            request.detailAddress(),
            request.requestMessage()
        );


        for(OrderItemCreateInfo orderItemCreateInfo : orderItemCreateInfoList) {
            OrderItem orderItem = OrderItem.create(
                order,
                orderItemCreateInfo.productId(),
                orderItemCreateInfo.producerId(),
                orderItemCreateInfo.quantity(),
                orderItemCreateInfo.unitPrice()
            );

            order.addOrderItem(orderItem);
        }

        order.calculateAmounts();
        return order;
    }



    private void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
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
