package com.high.order.domain.entity;

import com.high.order.domain.vo.DeliveryStatus;
import com.high.order.domain.vo.OrderItemStatus;
import com.library.jpa.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderItemId;

    private UUID productId;

    private UUID producerId;

    private Integer quantity;

    private Integer unitPrice;

    private Integer itemTotalPrice;

    @Enumerated(EnumType.STRING)
    private OrderItemStatus orderItemStatus;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private OrderItem(UUID productId, UUID producerId,
        Integer quantity, Integer unitPrice) {

        this.productId = productId;
        this.producerId = producerId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.orderItemStatus = OrderItemStatus.CREATED;
        this.deliveryStatus = DeliveryStatus.READY;
        this.itemTotalPrice = 0;

    }


    public static OrderItem create(UUID productId, UUID producerId,
        Integer quantity, Integer unitPrice) {
        OrderItem orderItem = new OrderItem(productId, producerId, quantity, unitPrice);
            orderItem.calculateAmounts();
        return orderItem;
    }


    private void calculateAmounts() {
        this.itemTotalPrice = this.quantity * this.unitPrice;

    }

    void setOrder(Order order) {
        this.order = order;
    }
}
