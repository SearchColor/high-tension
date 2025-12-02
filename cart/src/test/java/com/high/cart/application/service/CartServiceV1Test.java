package com.high.cart.application.service;

import com.high.cart.application.dto.request.CreateCartRequestDto;
import com.high.cart.application.dto.response.CartResponseDto;
import com.high.cart.application.dto.response.CreateCartResponseDto;
import com.high.cart.domain.exception.CartNotFoundException;
import com.high.cart.domain.model.Cart;
import com.high.cart.domain.model.CartItem;
import com.high.cart.domain.repository.CartRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("ID로 장바구니 조회_성공")
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
        Cart updatedCart = cartService.addItemToCart(TEST_USER_ID, SAMPLE_ITEM);

        // Then
        // save 메서드가 1번 호출되었는지 검증
        verify(cartRepository, times(1)).save(existingCart);
        // 장바구니에 아이템이 추가되었는지 검증 (Domain 로직이 성공적으로 작동했는지 간접 검증)
        assertThat(updatedCart.getItems()).hasSize(1);
        assertThat(updatedCart.getItems().get(0).getProductId()).isEqualTo(SAMPLE_ITEM.getProductId());
    }

    @Test
    @DisplayName("장바구니에 항목 추가_새 장바구니를 생성할 때")
    void addItemToCart_NewCartCreation() {
        // Given
        // Mocking: findByUserId가 Optional.empty()를 반환하도록 설정 (새 장바구니 생성 유도)
        when(cartRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.empty());

        // Mocking: save가 호출될 때 저장된 Cart 객체를 반환하도록 설정
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Cart newCart = cartService.addItemToCart(TEST_USER_ID, SAMPLE_ITEM);

        // Then
        // save 메서드가 1번 호출되었는지 검증
        verify(cartRepository, times(1)).save(any(Cart.class));
        // 새로 생성된 장바구니인지 검증
        assertThat(newCart).isNotNull();
        assertThat(newCart.getUserId()).isEqualTo(TEST_USER_ID);
        // 아이템이 추가되었는지 검증
        assertThat(newCart.getItems()).hasSize(1);
    }
}