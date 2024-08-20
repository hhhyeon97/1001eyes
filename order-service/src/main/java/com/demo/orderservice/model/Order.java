package com.demo.orderservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne
//    @JoinColumn(name = "user_id")
//    private User user;
 //프라이머리키로 user_id
 //private Long user 이런식으로 가고 manytoOne 빼고 !!!!!!!!!!!!
    private String userId;


    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> items = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    private LocalDateTime returnRequestDate;

    private Integer totalPrice;

    // 주문 상태 업데이트 메서드
    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }
}