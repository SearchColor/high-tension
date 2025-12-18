package com.high.order.domain.entity;

import com.high.order.domain.exception.InvalidOrderStateException;
import com.high.order.domain.exception.OrderCancellationException;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
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
        System.out.println("[Order] 총 금액 계산 완료 : " + this.itemTotalPrice);
    }

    public int calculateCancelAmounts(BigDecimal discountPercent) {
        BigDecimal price = BigDecimal.valueOf(itemTotalPrice);
        BigDecimal discountRate = discountPercent.divide(new BigDecimal("100"), 4,
            RoundingMode.HALF_UP);
        BigDecimal discountAmountBd = price.multiply(discountRate);
        BigDecimal finalPrice = price.subtract(discountAmountBd);

        return finalPrice.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    public boolean isCancellable() {
        return (this.orderItemStatus == OrderItemStatus.CREATED || this.orderItemStatus == OrderItemStatus.SUCCESS)
            && this.deliveryStatus == DeliveryStatus.READY;
    }

    public void updateItemStatus(OrderItemStatus nextStatus) {
        if (!this.orderItemStatus.canTransitionTo(nextStatus)) {
            System.out.println("[orderItem] 상태를 업데이트할 수 없음");
            throw new InvalidOrderStateException();
        }
        System.out.println("[orderItem] 상태 업데이트 : " + nextStatus);
        this.orderItemStatus = nextStatus;
    }

    public void updateDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }


    public void cancel() {
        if (!isCancellable()) {
            System.out.println("[orderItem] 취소할 수 없는 주문 상품");
            throw new OrderCancellationException();
        }
        this.orderItemStatus = OrderItemStatus.CANCELED;
    }

    void setOrder(Order order) {
        this.order = order;
    }
}
