package com.demo.myshop.service;

import com.demo.myshop.dto.ProductRequestDto;
import com.demo.myshop.model.Product;
import com.demo.myshop.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

//    // 상품 등록
//    public void saveProduct(Product product) {
//        productRepository.save(product);
//    }

// 상품 등록
public void saveProduct(ProductRequestDto productRequestDto) {
    Product product = new Product();
    product.setTitle(productRequestDto.getTitle());
    product.setDescription(productRequestDto.getDescription());
    product.setCategory(productRequestDto.getCategory());
    product.setPrice(productRequestDto.getPrice());
    product.setStock(productRequestDto.getStock());
    product.setImageUrl(productRequestDto.getImageUrl());
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

}
