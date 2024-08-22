package com.demo.productservice.service;

import com.demo.productservice.dto.ProductRequestDto;
import com.demo.productservice.dto.ProductResponseDto;
import com.demo.productservice.model.Product;
import com.demo.productservice.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserService userService;
    public ProductService(ProductRepository productRepository, UserService userService) {
        this.productRepository = productRepository;
        this.userService = userService;
    }

    // 상품 등록
    public void saveProduct(ProductRequestDto requestDto) {
        // DTO에서 엔티티로 변환 후 저장
        Product product = requestDto.toProduct();
        productRepository.save(product);
    }

    // 상품 리스트 조회
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // 상품 상세
    public Optional<Product> findItemById(Long id) {
        return productRepository.findById(id);
    }





    public String test(String text) {
        System.out.println("여기는 상품 서비스다 !!!!!!!");
        return userService.test(text);
    }

    // 오더 주문 취소시 재고 업데이트할 때 쓸 새로운 상품 엔티티 저장 메서드
    // todo : 이거 위에 상품 등록 메서드랑 중복되는것같음 ?!,,일단 돌아가게만 먼저 해보기
//
//    public void saveProduct2(ProductRequestDto productRequestDto) {
//        Product product = productRequestDto.toProduct();
//        productRepository.save(product);
//    }
    // 상품 저장 메서드 (Product 엔티티를 직접 저장)
    public void saveProduct(Product product) {
        productRepository.save(product);
    }

}
