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
import org.springframework.transaction.annotation.Transactional;

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
        return CartResponseDto.from(getCartOrThrow(userId));
    }

    @Transactional
    public CartResponseDto addItemToCart(String userId, CartItem item) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> Cart.builder().userId(userId).build());
        cart.addItem(item);
        return saveAndConvertToDto(cart);
    }

    @Transactional
    public CartResponseDto deleteItemToCart(String userId, String productId){
        Cart cart = getCartOrThrow(userId);
        cart.deleteItem(productId);
        return saveAndConvertToDto(cart);
    }

    @Transactional
    public CartResponseDto deleteAllToCart(String userId){
        Cart cart = getCartOrThrow(userId);
        cart.deleteAll();
        return saveAndConvertToDto(cart);
    }



    private void validateCartNotExists(String userId) {
        if (cartRepository.existsByUserId(userId)) {
            throw new CartAlreadyExistsException(userId);
        }
    }

    private Cart getCartOrThrow(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));
    }

    private CartResponseDto saveAndConvertToDto(Cart cart) {
        Cart savedCart = cartRepository.save(cart);
        return CartResponseDto.from(savedCart);
    }

}
