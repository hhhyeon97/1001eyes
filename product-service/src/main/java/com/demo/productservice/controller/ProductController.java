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
            return ResponseEntity.status(404).body(null);
        }
    }

//    // 남은 수량 조회
//    @GetMapping("/{id}/stock")
//    public ResponseEntity<Integer> getRemainingStock(@PathVariable("id") Long id) {
//        int remainingStock = productService.getStockFromRedis(id);
//        return ResponseEntity.ok(remainingStock);
//    } -> db 수량 (찐 재고 수량) 말고 임시 재고 수량으로 보여주기

    // order-service와 내부 소통 -> 주문 생성시, 상품 반품 완료시 재고 업데이트 api
    @PutMapping("/{id}/stock")
    public ResponseEntity<String> updateProductStock(@PathVariable("id") Long id, @RequestParam("stock") int stock) {
        productService.updateStockInRedis(id, stock);
        // 기존 DB 업데이트 로직 유지
        Optional<Product> productOpt = productService.findItemById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setStock(stock);
            productService.updateStock(product);
            return ResponseEntity.ok("재고가 성공적으로 업데이트되었습니다.");
        } else {
            return ResponseEntity.status(404).body("상품을 찾을 수 없습니다.");
        }
    }


}