package com.high.cart.domain.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Document(collection = "carts") // MongoDB 컬렉션 이름 지정
@Getter
@Builder
public class Cart {

    @Id
    private String id;
    @Indexed(unique = true)
    private final String userId;
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();
    @Builder.Default
    private long totalPrice = 0;

    public static Cart create(String userId, List<CartItem> items){
         Cart cart = Cart.builder()
                .userId(userId)
                .items(items)
                .build();
        cart.calculateTotalPrice();
        return cart;
    }

    public void addItem(CartItem newItem) {
        // 기존 항목 목록에서 같은 상품이 있는지 찾습니다.
        Optional<CartItem> existingItemOptional = this.items.stream()
                .filter(item -> item.getProductId().equals(newItem.getProductId()))
                .findFirst();

        if (existingItemOptional.isPresent()) {
            // 같은 상품이 있으면 수량과 가격을 업데이트합니다.
            CartItem existingItem = existingItemOptional.get();
            int newQuantity = existingItem.getQuantity() + newItem.getQuantity();
            existingItem.setQuantity(newQuantity);
            existingItem.setPrice(newItem.getPrice());
        } else {
            this.items.add(newItem);
        }
        this.calculateTotalPrice();
    }

    public void deleteItem(String productId){
        Optional<CartItem> existingItemOptional = this.items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
        existingItemOptional.ifPresent(item -> this.items.remove(item));
        this.calculateTotalPrice();
    }

    public void deleteAll(){
        this.items.clear();
        this.calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        this.totalPrice = this.items.stream()
                .mapToLong(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
}