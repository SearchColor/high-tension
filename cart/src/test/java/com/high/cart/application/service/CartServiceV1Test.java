package com.high.cart.application.service;

import com.high.cart.application.dto.request.CreateCartRequestDto;
import com.high.cart.application.dto.response.CartResponseDto;
import com.high.cart.application.dto.response.CreateCartResponseDto;
import com.high.cart.domain.exception.CartAlreadyExistsException;
import com.high.cart.domain.exception.CartNotFoundException;
import com.high.cart.domain.model.Cart;
import com.high.cart.domain.model.CartItem;
import com.high.cart.domain.repository.CartRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceV1Test {

    @InjectMocks
    private CartServiceV1 cartService;
    @Mock
    private CartRepository cartRepository;

    private final String TEST_USER_ID = "testUser123";
    private final CartItem SAMPLE_ITEM =
            CartItem.builder()
                    .productId("P001")
                    .quantity(2)
                    .price(10000)
                    .build();
    private final CartItem SAMPLE_ITEM2 =
            CartItem.builder()
                    .productId("P002")
                    .quantity(1)
                    .price(20000)
                    .build();


    @Nested
    @DisplayName("저장 관련 테스트")
    class TestCreate {

        @Test
        @DisplayName("장바구니 저장 성공 테스트")
        void saveCart_Success() {
            // given
            String userId = "1";
            List<CartItem> items = Arrays.asList(SAMPLE_ITEM, SAMPLE_ITEM2);

            CreateCartRequestDto requestDto = CreateCartRequestDto.builder()
                    .userId(userId)
                    .items(items)
                    .build();

            Cart savedCart = Cart.builder()
                    .userId(userId)
                    .items(items)
                    .build();

            when(cartRepository.save(any(Cart.class))).thenReturn(savedCart);

            // when
            CreateCartResponseDto response = cartService.saveCart(requestDto);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(userId);
            assertThat(response.getItems()).hasSize(2);

            verify(cartRepository, times(1)).existsByUserId(userId);
            verify(cartRepository, times(1)).save(any(Cart.class));
        }

        @Test
        @DisplayName("장바구니 저장 실패 - 이미 존재하는 장바구니")
        void saveCart_Failure_AlreadyExists() {
            // given
            String userId = "user123";
            List<CartItem> items = Collections.singletonList(SAMPLE_ITEM);

            CreateCartRequestDto requestDto = CreateCartRequestDto.builder()
                    .userId(userId)
                    .items(items)
                    .build();

            when(cartRepository.existsByUserId(userId)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> cartService.saveCart(requestDto))
                    .isInstanceOf(CartAlreadyExistsException.class);

            verify(cartRepository, times(1)).existsByUserId(userId);
            verify(cartRepository, never()).save(any(Cart.class));
        }

        @Test
        @DisplayName("빈 장바구니 저장 성공")
        void saveCart_Success_EmptyCart() {
            // given
            String userId = "user123";
            List<CartItem> emptyItems = new ArrayList<>();

            CreateCartRequestDto requestDto = CreateCartRequestDto.builder()
                    .userId(userId)
                    .items(emptyItems)
                    .build();

            Cart savedCart = Cart.builder()
                    .userId(userId)
                    .items(emptyItems)
                    .build();

            when(cartRepository.existsByUserId(userId)).thenReturn(false);
            when(cartRepository.save(any(Cart.class))).thenReturn(savedCart);

            // when
            CreateCartResponseDto response = cartService.saveCart(requestDto);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getItems()).isEmpty();
            assertThat(response.getTotalPrice()).isZero();
        }

    }

    @Nested
    @DisplayName("조회 관련 테스트")
    class TestRead {
        @Test
        @DisplayName("ID로 장바구니 조회_성공")
        @Tag("조회")
        void getCartByUserId_Success() {
            // Given
            Cart mockCart = Cart.builder().userId(TEST_USER_ID).build();
            // Repository 호출 시 Optional.of(Cart)를 반환하도록 설정
            when(cartRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(mockCart));

            // When
            CartResponseDto responseDto = cartService.getCartByUserId(TEST_USER_ID);

            // Then
            assertThat(responseDto).isNotNull();
            assertThat(responseDto.getUserId()).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("ID로 장바구니 조회_실패_CartNotFoundException 발생")
        @Tag("조회")
        void getCartByUserId_Failure_ThrowsException() {
            // Given
            // Repository 호출 시 Optional.empty()를 반환하도록 설정
            when(cartRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.empty());

            // When & Then
            // CartNotFoundException이 발생하는지 검증
            assertThrows(CartNotFoundException.class, () -> {
                cartService.getCartByUserId(TEST_USER_ID);
            });
        }
    }

    @Nested
    @DisplayName("수정 관련 테스트")
    class TestUpdate {
        @Test
        @DisplayName("장바구니에 항목 추가_기존 장바구니가 있을 때")
        void addItemToCart_ExistingCart() {
            // Given
            Cart existingCart = Cart.builder().userId(TEST_USER_ID).build();
            // Mocking: 기존 장바구니가 조회되도록 설정
            when(cartRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(existingCart));
            // Mocking: 저장 시 기존 장바구니를 반환하도록 설정
            when(cartRepository.save(any(Cart.class))).thenReturn(existingCart);

            // When
            CartResponseDto updatedCart = cartService.addItemToCart(TEST_USER_ID, SAMPLE_ITEM);

            // Then
            // save 메서드가 1번 호출되었는지 검증
            verify(cartRepository, times(1)).save(existingCart);
            // 장바구니에 아이템이 추가되었는지 검증 (Domain 로직이 성공적으로 작동했는지 간접 검증)
            assertThat(updatedCart.getItems()).hasSize(1);
            assertThat(updatedCart.getItems().get(0).getProductId()).isEqualTo(SAMPLE_ITEM.getProductId());
        }

        @Test
        @DisplayName("장바구니에 항목 추가 - 동일 상품 수량 증가")
        void addItemToCart_SameProduct_QuantityIncrease() {
            // given
            List<CartItem> existingItems = new ArrayList<>();
            existingItems.add(CartItem.builder()
                    .productId("P001")
                    .quantity(1)
                    .price(10000)
                    .build());

            Cart existingCart = Cart.builder()
                    .userId(TEST_USER_ID)
                    .items(existingItems)
                    .build();

            CartItem additionalItem = CartItem.builder()
                    .productId("P001")
                    .quantity(2)
                    .price(10000)
                    .build();

            when(cartRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(existingCart));
            when(cartRepository.save(any(Cart.class))).thenReturn(existingCart);

            // when
            CartResponseDto response = cartService.addItemToCart(TEST_USER_ID, additionalItem);

            // then
            assertThat(response.getItems()).hasSize(1);
            assertThat(response.getItems().get(0).getQuantity()).isEqualTo(3); // 1 + 2
        }
    }

    @Nested
    @DisplayName("삭제 관련 테스트")
    class TestDelete {

        @Test
        @DisplayName("장바구니에서 항목 삭제 성공")
        void deleteItemToCart_Success() {
            // given
            List<CartItem> items = new ArrayList<>();
            items.add(SAMPLE_ITEM);
            items.add(SAMPLE_ITEM2);

            Cart existingCart = Cart.builder()
                    .userId(TEST_USER_ID)
                    .items(items)
                    .build();

            when(cartRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(existingCart));
            when(cartRepository.save(any(Cart.class))).thenReturn(existingCart);

            // when
            CartResponseDto response = cartService.deleteItemToCart(TEST_USER_ID, SAMPLE_ITEM.getProductId());

            // then
            verify(cartRepository, times(1)).findByUserId(TEST_USER_ID);
            verify(cartRepository, times(1)).save(existingCart);
            assertThat(response.getItems()).hasSize(1);
        }

        @Test
        @DisplayName("장바구니에서 항목 삭제 실패 - 장바구니 없음")
        void deleteItemToCart_Failure_CartNotFound() {
            // given
            when(cartRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cartService.deleteItemToCart(TEST_USER_ID, SAMPLE_ITEM.getProductId()))
                    .isInstanceOf(CartNotFoundException.class);

            verify(cartRepository, times(1)).findByUserId(TEST_USER_ID);
            verify(cartRepository, never()).save(any(Cart.class));
        }

        @Test
        @DisplayName("장바구니에서 존재하지 않는 항목 삭제 시도")
        void deleteItemToCart_NonExistentItem() {
            // given
            Cart existingCart = Cart.builder()
                    .userId(TEST_USER_ID)
                    .items(new ArrayList<>(Arrays.asList(SAMPLE_ITEM)))
                    .build();

            CartItem nonExistentItem = CartItem.builder()
                    .productId("P999")
                    .quantity(1)
                    .price(5000)
                    .build();

            when(cartRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(existingCart));
            when(cartRepository.save(any(Cart.class))).thenReturn(existingCart);

            // when
            CartResponseDto response = cartService.deleteItemToCart(TEST_USER_ID, nonExistentItem.getProductId());

            // then
            assertThat(response.getItems()).hasSize(1); // 원래 아이템 유지
            verify(cartRepository, times(1)).save(existingCart);
        }

        // ==================== deleteAllToCart 테스트 ====================

        @Test
        @DisplayName("장바구니 전체 삭제 성공")
        void deleteAllToCart_Success() {
            // given
            List<CartItem> items = new ArrayList<>();
            items.add(SAMPLE_ITEM);
            items.add(SAMPLE_ITEM2);

            Cart existingCart = Cart.builder()
                    .userId(TEST_USER_ID)
                    .items(items)
                    .build();

            when(cartRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(existingCart));
            when(cartRepository.save(any(Cart.class))).thenReturn(existingCart);

            // when
            CartResponseDto response = cartService.deleteAllToCart(TEST_USER_ID);

            // then
            verify(cartRepository, times(1)).findByUserId(TEST_USER_ID);
            verify(cartRepository, times(1)).save(existingCart);
            assertThat(response.getItems()).isEmpty();
            assertThat(response.getTotalPrice()).isZero();
        }

        @Test
        @DisplayName("장바구니 전체 삭제 실패 - 장바구니 없음")
        @Tag("삭제")
        void deleteAllToCart_Failure_CartNotFound() {
            // given
            when(cartRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cartService.deleteAllToCart(TEST_USER_ID))
                    .isInstanceOf(CartNotFoundException.class);

            verify(cartRepository, times(1)).findByUserId(TEST_USER_ID);
            verify(cartRepository, never()).save(any(Cart.class));
        }

        @Test
        @DisplayName("이미 비어있는 장바구니 전체 삭제")
        void deleteAllToCart_AlreadyEmptyCart() {
            // given
            Cart emptyCart = Cart.builder()
                    .userId(TEST_USER_ID)
                    .items(new ArrayList<>())
                    .build();

            when(cartRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(emptyCart));
            when(cartRepository.save(any(Cart.class))).thenReturn(emptyCart);

            // when
            CartResponseDto response = cartService.deleteAllToCart(TEST_USER_ID);

            // then
            assertThat(response.getItems()).isEmpty();
            assertThat(response.getTotalPrice()).isZero();
            verify(cartRepository, times(1)).save(emptyCart);
        }
    }

    @Test
    @DisplayName("통합 시나리오 - 장바구니 생성, 추가, 삭제, 조회")
    void integrationScenario_CreateAddDeleteGet() {
        // given
        String userId = "user123";
        List<CartItem> items = Collections.singletonList(SAMPLE_ITEM);

        CreateCartRequestDto requestDto = CreateCartRequestDto.builder()
                .userId(userId)
                .items(items)
                .build();

        Cart cart = Cart.builder()
                .userId(userId)
                .items(new ArrayList<>(items))
                .build();

        // 장바구니 저장
        when(cartRepository.existsByUserId(userId)).thenReturn(false);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        // when
        CreateCartResponseDto createResponse = cartService.saveCart(requestDto);
        CartResponseDto addResponse = cartService.addItemToCart(userId, SAMPLE_ITEM2);
        CartResponseDto getResponse = cartService.getCartByUserId(userId);

        // then
        assertThat(createResponse).isNotNull();
        assertThat(addResponse.getItems()).hasSize(2);
        assertThat(getResponse.getUserId()).isEqualTo(userId);
    }
}