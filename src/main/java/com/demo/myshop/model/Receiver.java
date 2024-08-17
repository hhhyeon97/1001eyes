package com.demo.myshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "receivers")
public class Receiver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String receiverName; // 받는 분 성함
    private String receiverPhoneNumber; // 받는 분 전화번호

    @OneToOne(mappedBy = "receiver")
    private Address address; // 주소와 1:1 관계

    // 생성자
    public Receiver(String receiverName, String receiverPhoneNumber) {
        this.receiverName = receiverName;
        this.receiverPhoneNumber = receiverPhoneNumber;
    }
}