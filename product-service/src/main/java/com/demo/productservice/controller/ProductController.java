package com.demo.productservice.controller;


import com.demo.productservice.dto.ProductRequestDto;
import com.demo.productservice.dto.ProductResponseDto;
import com.demo.productservice.model.Product;
import com.demo.productservice.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // 상품 등록
    @PostMapping
    public ResponseEntity<String> addProduct(@RequestBody ProductRequestDto productRequestDto) {
        productService.saveProduct(productRequestDto);
        return ResponseEntity.ok("상품 등록 완료!");
    }

    // 상품 리스트 조회
    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> productList() {
        List<ProductResponseDto> products = productService.getAllProducts()
                .stream()
                .map(ProductResponseDto::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    // 상품 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> productDetail(@PathVariable("id") Long id) {
        Optional<Product> result = productService.findItemById(id);

        if (result.isPresent()) {
            ProductResponseDto productDto = new ProductResponseDto(result.get());
            return ResponseEntity.ok(productDto);
        } else {
            return ResponseEntity.status(404).body(null); // or use a more detailed error message
        }
    }
}