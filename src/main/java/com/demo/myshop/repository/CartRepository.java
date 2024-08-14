package com.demo.myshop.repository;


import com.demo.myshop.model.Cart;
import com.demo.myshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);
}