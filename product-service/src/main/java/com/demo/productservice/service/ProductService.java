package com.demo.productservice.service;

import com.demo.productservice.dto.ProductRequestDto;
import com.demo.productservice.model.Product;
import com.demo.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // 상품 등록
    public void saveProduct(ProductRequestDto requestDto) {
        // dto에서 엔티티로 변환 후 저장
        Product product = requestDto.toProduct();
        productRepository.save(product);
    }

    // 상품 리스트 조회
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // 상품 상세 조회
    public Optional<Product> findItemById(Long id) {
        return productRepository.findById(id);
    }

}
