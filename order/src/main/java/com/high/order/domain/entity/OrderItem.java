package com.high.order.domain.entity;

import com.high.order.domain.vo.DeliveryStatus;
import com.high.order.domain.vo.OrderItemStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "p_orderItem")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderItemId;

    private UUID productId;

    private UUID producerId;

    private Integer quantity;

    private Integer unitPrice;

    @Enumerated(EnumType.STRING)
    private OrderItemStatus orderItemStatus;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;
}
