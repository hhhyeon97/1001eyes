package com.demo.orderservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @OneToOne
//    @JoinColumn(name = "user_id")
//    private User user;
    private String userId;

    private Integer totalPrice = 0; // 총 가격

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItem> items = new HashSet<>();

////    // 카트의 총 가격을 업데이트하는 메서드
//    public void updateTotalPrice() {
//        totalPrice = items.stream()
//                .mapToInt(item -> item.getProduct().getPrice() * item.getQuantity())
//                .sum();
//    }

}