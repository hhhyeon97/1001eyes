package com.demo.productservice.repository;

import com.demo.productservice.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 비관적 락을 사용하여 재고를 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(Long id);

//    // 기존 findById 메서드를 변경한 이름으로 정의
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("SELECT p FROM Product p WHERE p.id = :id")
//    Optional<Product> findProductByIdWithLock(@Param("id") Long id);

}
