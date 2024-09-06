package com.demo.productservice.controller;


import com.demo.productservice.dto.ProductListResponseDto;
import com.demo.productservice.dto.ProductRequestDto;
import com.demo.productservice.dto.ProductResponseDto;
import com.demo.productservice.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
    public ResponseEntity<List<ProductListResponseDto>> productList() {
        List<ProductListResponseDto> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }


    // 상품 상세 조회 -> 수정한 사항 : 상세페이지에서 보여줄 재고는 레디스 캐싱한 데이터
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> productDetail(@PathVariable("id") Long id) {
        Optional<ProductResponseDto> result = productService.findItemDetailById(id);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            return ResponseEntity.status(404).body(null);
        }
    }



    /**
     * order-service와 내부 소통하는 api
     * 실제 상품 db 재고 업데이트
     */
//    @PutMapping("/{id}/stock")
//    public ResponseEntity<String> updateProductStock(@PathVariable("id") Long id, @RequestParam("stock") int stock) {
//        try {
//            // 서비스 레이어를 통해 재고 업데이트
//            productService.updateProductStock(id, stock);
//            return ResponseEntity.ok("재고가 성공적으로 업데이트되었습니다.");
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body("재고 업데이트 중 오류 발생: " + e.getMessage());
//        }
//    }

    /**
     *  order-service와 소통하는 내부 api
     *  -> db 재고만 조회해서 반환해줄 api !!
    * */
    // 상품의 stock 수량 조회 API
    @GetMapping("/internal/{id}")
    public ResponseEntity<Integer> getProductStock(@PathVariable("id") Long id) {
        // ProductService에서 stock 정보를 가져옴
        int stock = productService.getProductStock(id);
        // 반환값 설정 (stock 수량)
        return ResponseEntity.ok(stock);
    }

    /** 240831
     * 주문에서 호출하는 재고 확인 및 차감 API
     * 재고가 충분하면 차감하고, 부족하면 예외를 발생시킴
     */
    @PostMapping("/{id}/deduct")
    public ResponseEntity<String> checkAndDeductStock(@PathVariable("id") Long id, @RequestParam("quantity") int quantity) {
        try {
            productService.checkAndDeductStock(id, quantity);
            return ResponseEntity.ok("재고 차감이 완료되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("재고 차감 중 오류 발생: " + e.getMessage());
        }
    }



}