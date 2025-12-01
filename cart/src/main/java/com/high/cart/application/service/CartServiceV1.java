package com.high.cart.application.service;

import com.high.cart.application.dto.request.CreateCartRequestDto;
import com.high.cart.application.dto.response.CartResponseDto;
import com.high.cart.application.dto.response.CreateCartResponseDto;
import com.high.cart.domain.exception.CartAlreadyExistsException;
import com.high.cart.domain.exception.CartNotFoundException;
import com.high.cart.domain.model.Cart;
import com.high.cart.domain.model.CartItem;
import com.high.cart.domain.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CartServiceV1 {

    private final CartRepository cartRepository;

    public CreateCartResponseDto saveCart(CreateCartRequestDto requestDto){
        validateCartNotExists(requestDto.getUserId());
        Cart cart = Cart.create(requestDto.getUserId(), requestDto.getItems());
        cartRepository.save(cart);
        return CreateCartResponseDto.from(cart);
    }

    public CartResponseDto getCartByUserId(String userId){
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(()-> new CartNotFoundException(userId));
        return CartResponseDto.from(cart);
    }

    public Cart addItemToCart(String userId, CartItem item) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> Cart.builder().userId(userId).build());
        cart.addItem(item);
        cartRepository.save(cart);
        return cart;
    }


    private void validateCartNotExists(String userId) {
        if (cartRepository.existsByUserId(userId)) {
            throw new CartAlreadyExistsException(userId);
        }
    }
}
