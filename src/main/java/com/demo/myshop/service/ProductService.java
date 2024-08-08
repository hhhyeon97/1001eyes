package com.demo.myshop.service;

import com.demo.myshop.model.Product;
import com.demo.myshop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // 상품 등록
    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    // 상품 리스트 조회
    public List<Product> getAvailableProducts() {
        return productRepository.findByIsDeletedFalse();
    }

    // 상품 상세
    public Optional<Product> findItemById(Long id) {
        return productRepository.findById(id);
    }

}
