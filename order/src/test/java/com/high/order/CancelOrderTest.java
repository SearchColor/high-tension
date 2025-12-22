package com.high.order;

import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@DisplayName("주문 취소 테스트")
public class CancelOrderTest {
//
//
//    @Autowired
//    private OrderService orderService;
//
//    @Autowired
//    private OrderRepository orderRepository;
//
//    @Autowired
//    private OrderItemRepository orderItemRepository;
//
//    @Autowired
//    private EntityManager entityManager;
//
//    private UUID testCustomerId;
//
//    @BeforeEach
//    void setUp() {
//        testCustomerId = UUID.randomUUID();
//    }
//
//    // 테스트용 Order 생성 헬퍼 메서드
//    private Order createTestOrder(OrderStatus orderStatus,
//        OrderItemStatus itemStatus,
//        DeliveryStatus deliveryStatus) {
//        // OrderItem 생성
//        OrderItem orderItem = OrderItem.create(
//            UUID.randomUUID(), // productId
//            UUID.randomUUID(), // producerId
//            2,                 // quantity
//            10000             // unitPrice
//        );
//
//        // Order 생성
//        Order order = Order.createOrder(
//            testCustomerId,
//            null,
//            "테스트 수령인",
//            "010-1234-5678",
//            "서울시 강남구",
//            "101동 101호",
//            "테스트 주문",
//            List.of(orderItem),
//            BigDecimal.valueOf(20)
//        );
//
//        Order savedOrder = orderRepository.save(order);
//        entityManager.flush();
//        entityManager.clear();
//
//        // 상태 강제 변경 (테스트용)
//        Order managedOrder = orderRepository.findById(savedOrder.getOrderId()).get();
//        setOrderStatus(managedOrder, orderStatus);
//        setOrderItemStatus(managedOrder.getOrderItems().get(0), itemStatus);
//        setDeliveryStatus(managedOrder.getOrderItems().get(0), deliveryStatus);
//
//        orderRepository.save(managedOrder);
//        entityManager.flush();
//        entityManager.clear();
//
//        return orderRepository.findById(savedOrder.getOrderId()).get();
//    }
//
//    // Reflection을 사용한 상태 강제 설정 (테스트용)
//    private void setOrderStatus(Order order, OrderStatus status) {
//        try {
//            var field = Order.class.getDeclaredField("orderStatus");
//            field.setAccessible(true);
//            field.set(order, status);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private void setOrderItemStatus(OrderItem item, OrderItemStatus status) {
//        try {
//            var field = OrderItem.class.getDeclaredField("orderItemStatus");
//            field.setAccessible(true);
//            field.set(item, status);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private void setDeliveryStatus(OrderItem item, DeliveryStatus status) {
//        try {
//            var field = OrderItem.class.getDeclaredField("deliveryStatus");
//            field.setAccessible(true);
//            field.set(item, status);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    // ==================== 전체 주문 취소 테스트 ====================
//
//    @Nested
//    @DisplayName("전체 주문 취소 - 성공 케이스")
//    class CancelOrderSuccessTests {
//
//        @Test
//        @DisplayName("주문 상태: CREATED, 아이템 상태: CREATED, 배송 상태: READY -> 취소 성공")
//        void cancelOrder_CreatedOrder_CreatedItem_ReadyDelivery_Success() {
//            // Given
//            Order order = createTestOrder(
//                OrderStatus.CREATED,
//                OrderItemStatus.CREATED,
//                DeliveryStatus.READY
//            );
//            UUID orderId = order.getOrderId();
//
//            // When
//            OrderResponse response = orderService.cancelOrder(orderId);
//
//            // Then
//            assertThat(response.orderId()).isEqualTo(orderId); // orderId만 검증
//
//            Order canceledOrder = orderRepository.findById(orderId)
//                .orElseThrow(() -> new AssertionError("주문을 찾을 수 없습니다"));
//
//            assertThat(canceledOrder.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
//            assertThat(canceledOrder.getOrderItems())
//                .isNotEmpty()
//                .allMatch(item -> item.getOrderItemStatus() == OrderItemStatus.CANCELED);
//        }
//
//        @Test
//        @DisplayName("주문 상태: SUCCESS, 아이템 상태: SUCCESS, 배송 상태: READY -> 취소 성공")
//        void cancelOrder_SuccessOrder_SuccessItem_ReadyDelivery_Success() {
//            // Given
//            Order order = createTestOrder(
//                OrderStatus.SUCCESS,
//                OrderItemStatus.SUCCESS,
//                DeliveryStatus.READY
//            );
//            UUID orderId = order.getOrderId();
//
//            // When
//            OrderResponse response = orderService.cancelOrder(orderId);
//
//            // Then
//            Order canceledOrder = orderRepository.findById(orderId)
//                .orElseThrow(() -> new AssertionError("주문을 찾을 수 없습니다"));
//
//            assertThat(canceledOrder.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
//        }
//    }
//
//    @Nested
//    @DisplayName("전체 주문 취소 - 실패 케이스")
//    class CancelOrderFailureTests {
//
//        @Test
//        @DisplayName("주문 상태: CANCELED -> 취소 실패 (이미 취소됨)")
//        void cancelOrder_AlreadyCanceled_Fail() {
//            // Given
//            Order order = createTestOrder(
//                OrderStatus.CANCELED,
//                OrderItemStatus.CANCELED,
//                DeliveryStatus.READY
//            );
//
//            // When & Then
//            assertThatThrownBy(() -> orderService.cancelOrder(order.getOrderId()))
//                .isInstanceOf(OrderBadRequestException.class);
//        }
//
//        @Test
//        @DisplayName("주문 상태: SUCCESS, 아이템 상태: SUCCESS, 배송 상태: DELIVERED -> 취소 실패")
//        void cancelOrder_SuccessOrder_InDelivery_Fail() {
//            // Given
//            Order order = createTestOrder(
//                OrderStatus.SUCCESS,
//                OrderItemStatus.SUCCESS,
//                DeliveryStatus.DELIVERED
//            );
//
//            // When & Then
//            assertThatThrownBy(() -> orderService.cancelOrder(order.getOrderId()))
//                .isInstanceOf(OrderBadRequestException.class);
//        }
//
//        @Test
//        @DisplayName("존재하지 않는 주문 취소 시도 -> 실패")
//        void cancelOrder_NotFound_Fail() {
//            // Given
//            UUID nonExistentOrderId = UUID.randomUUID();
//
//            // When & Then
//            assertThatThrownBy(() -> orderService.cancelOrder(nonExistentOrderId))
//                .isInstanceOf(OrderNotFoundException.class);
//        }
//
//        @Test
//        @DisplayName("삭제된 주문 취소 시도 -> 실패")
//        void cancelOrder_DeletedOrder_Fail() {
//            // Given
//            Order order = createTestOrder(
//                OrderStatus.CREATED,
//                OrderItemStatus.CREATED,
//                DeliveryStatus.READY
//            );
//            order.softDelete(UUID.randomUUID());
//            orderRepository.save(order);
//            entityManager.flush();
//            entityManager.clear();
//
//            // When & Then
//            assertThatThrownBy(() -> orderService.cancelOrder(order.getOrderId()))
//                .isInstanceOf(OrderNotFoundException.class);
//        }
//    }
//
//    // ==================== 개별 아이템 취소 테스트 ====================
//
//    @Nested
//    @DisplayName("개별 주문 아이템 취소 - 성공 케이스")
//    class CancelOrderItemSuccessTests {
//
//        @Test
//        @DisplayName("주문 상태: CREATED, 아이템 상태: CREATED -> 개별 취소 성공")
//        void cancelOrderItem_CreatedOrder_CreatedItem_Success() {
//            // Given
//            Order order = createTestOrder(
//                OrderStatus.CREATED,
//                OrderItemStatus.CREATED,
//                DeliveryStatus.READY
//            );
//            UUID orderItemId = order.getOrderItems().get(0).getOrderItemId();
//
//            // When
//            OrderItemIdResponse response = orderService.cancelOrderItem(
//                order.getOrderId(),
//                orderItemId
//            );
//
//            // Then
//            assertThat(response.orderItemId()).isEqualTo(orderItemId);
//
//            OrderItem canceledItem = orderItemRepository.findByOrderItemIdAndDeletedAtIsNull(orderItemId).get();
//            assertThat(canceledItem.getOrderItemStatus()).isEqualTo(OrderItemStatus.CANCELED);
//        }
//    }
//
//    @Nested
//    @DisplayName("개별 주문 아이템 취소 - 실패 케이스")
//    class CancelOrderItemFailureTests {
//
//        @Test
//        @DisplayName("주문 상태: SUCCESS -> 개별 취소 실패 (결제 완료된 주문)")
//        void cancelOrderItem_SuccessOrder_Fail() {
//            // Given
//            Order order = createTestOrder(
//                OrderStatus.SUCCESS,
//                OrderItemStatus.SUCCESS,
//                DeliveryStatus.READY
//            );
//            UUID orderItemId = order.getOrderItems().get(0).getOrderItemId();
//
//            // When & Then
//            assertThatThrownBy(() ->
//                orderService.cancelOrderItem(order.getOrderId(), orderItemId))
//                .isInstanceOf(OrderBadRequestException.class);
//        }
//
//        @Test
//        @DisplayName("주문 아이템 상태: SUCCESS -> 개별 취소 실패")
//        void cancelOrderItem_SuccessItem_Fail() {
//            // Given
//            Order order = createTestOrder(
//                OrderStatus.CREATED,
//                OrderItemStatus.SUCCESS,
//                DeliveryStatus.READY
//            );
//            UUID orderItemId = order.getOrderItems().get(0).getOrderItemId();
//
//            // When & Then
//            assertThatThrownBy(() ->
//                orderService.cancelOrderItem(order.getOrderId(), orderItemId))
//                .isInstanceOf(OrderBadRequestException.class);
//        }
//
//        @Test
//        @DisplayName("주문 아이템 상태: CANCELED -> 개별 취소 실패 (이미 취소됨)")
//        void cancelOrderItem_AlreadyCanceled_Fail() {
//            // Given
//            Order order = createTestOrder(
//                OrderStatus.CREATED,
//                OrderItemStatus.CANCELED,
//                DeliveryStatus.READY
//            );
//            UUID orderItemId = order.getOrderItems().get(0).getOrderItemId();
//
//            // When & Then
//            assertThatThrownBy(() ->
//                orderService.cancelOrderItem(order.getOrderId(), orderItemId))
//                .isInstanceOf(OrderBadRequestException.class);
//        }
//
//        @Test
//        @DisplayName("존재하지 않는 주문 아이템 취소 시도 -> 실패")
//        void cancelOrderItem_ItemNotFound_Fail() {
//            // Given
//            Order order = createTestOrder(
//                OrderStatus.CREATED,
//                OrderItemStatus.CREATED,
//                DeliveryStatus.READY
//            );
//            UUID nonExistentItemId = UUID.randomUUID();
//
//            // When & Then
//            assertThatThrownBy(() ->
//                orderService.cancelOrderItem(order.getOrderId(), nonExistentItemId))
//                .isInstanceOf(OrderItemNotFoundException.class);
//        }
//    }
//
//    // ==================== 파라미터화된 테스트 ====================
//
//    @Nested
//    @DisplayName("파라미터화된 취소 테스트")
//    class ParameterizedCancelTests {
//
//        @ParameterizedTest
//        @DisplayName("다양한 주문/아이템 상태 조합 테스트")
//        @CsvSource({
//            "CREATED, CREATED, READY, true",
//            "CREATED, SUCCESS, READY, false",
//            "CREATED, CANCELED, READY, false",
//            "SUCCESS, SUCCESS, READY, true",
//            "SUCCESS, SUCCESS, DELIVERED, false",
//            "CANCELED, CANCELED, READY, false"
//        })
//        void testCancelOrderWithVariousStates(
//            OrderStatus orderStatus,
//            OrderItemStatus itemStatus,
//            DeliveryStatus deliveryStatus,
//            boolean shouldSucceed
//        ) {
//            // Given
//            Order order = createTestOrder(orderStatus, itemStatus, deliveryStatus);
//
//            // When & Then
//            if (shouldSucceed) {
//                assertThatCode(() -> orderService.cancelOrder(order.getOrderId()))
//                    .doesNotThrowAnyException();
//
//                Order canceledOrder = orderRepository.findById(order.getOrderId()).get();
//                assertThat(canceledOrder.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
//            } else {
//                assertThatThrownBy(() -> orderService.cancelOrder(order.getOrderId()))
//                    .isInstanceOf(RuntimeException.class);
//            }
//        }
//    }
//
//    // ==================== 복합 시나리오 테스트 ====================
//
//    @Nested
//    @DisplayName("복합 시나리오 테스트")
//    class ComplexScenarioTests {
//
//        @Test
//        @DisplayName("여러 아이템 중 일부만 취소 가능한 상태 -> 전체 취소 실패")
//        void cancelOrder_MixedCancellableItems_Fail() {
//            // Given
//            OrderItem item1 = OrderItem.create(
//                UUID.randomUUID(), UUID.randomUUID(), 1, 10000
//            );
//            OrderItem item2 = OrderItem.create(
//                UUID.randomUUID(), UUID.randomUUID(), 2, 20000
//            );
//
//            Order order = Order.createOrder(
//                testCustomerId, null, "테스트", "010-1234-5678",
//                "서울", "101호", "메시지", List.of(item1, item2), BigDecimal.valueOf(20)
//            );
//
//            orderRepository.save(order);
//            entityManager.flush();
//            entityManager.clear();
//
//            // item1은 배송 중으로 변경
//            Order managedOrder = orderRepository.findById(order.getOrderId()).get();
//            setDeliveryStatus(managedOrder.getOrderItems().get(0), DeliveryStatus.DELIVERED);
//            orderRepository.save(managedOrder);
//            entityManager.flush();
//            entityManager.clear();
//
//            // When & Then
//            assertThatThrownBy(() -> orderService.cancelOrder(order.getOrderId()))
//                .isInstanceOf(OrderBadRequestException.class);
//        }
//
//        @Test
//        @DisplayName("모든 아이템이 취소 가능한 상태 -> 전체 취소 성공")
//        void cancelOrder_AllCancellableItems_Success() {
//            // Given
//            OrderItem item1 = OrderItem.create(
//                UUID.randomUUID(), UUID.randomUUID(), 1, 10000
//            );
//            OrderItem item2 = OrderItem.create(
//                UUID.randomUUID(), UUID.randomUUID(), 2, 20000
//            );
//
//            Order order = Order.createOrder(
//                testCustomerId, null, "테스트", "010-1234-5678",
//                "서울", "101호", "메시지", List.of(item1, item2), BigDecimal.valueOf(20)
//            );
//
//            orderRepository.save(order);
//            UUID orderId = order.getOrderId();
//
//            // When
//            OrderResponse response = orderService.cancelOrder(orderId);
//
//            // Then
//            assertThat(response.orderId()).isEqualTo(orderId); // orderId만 검증
//
//
//            // DB에서 다시 조회하여 상태 검증
//            Order canceledOrder = orderRepository.findById(orderId)
//                .orElseThrow(() -> new AssertionError("주문을 찾을 수 없습니다"));
//
//            assertThat(canceledOrder.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
//            assertThat(canceledOrder.getOrderItems())
//                .isNotEmpty()
//                .allMatch(item -> item.getOrderItemStatus() == OrderItemStatus.CANCELED);
//        }
//    }
}
