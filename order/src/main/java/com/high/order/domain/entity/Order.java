package com.high.order.domain.entity;

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
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="p_order")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderId;

    private UUID customerId;

    private UUID couponId;

    private Integer totalPrice;

    private Integer discount_amount;

    private Integer paid_amount;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private String recipient;

    private String recipientContact;

    private String deliveryAddress;

    private String detailAddress;

    private String requestMessage;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

}
