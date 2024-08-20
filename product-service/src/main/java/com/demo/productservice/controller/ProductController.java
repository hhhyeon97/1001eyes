package com.demo.productservice.controller;


import com.demo.productservice.client.UserServiceClient;
import com.demo.productservice.core.ApiUtils;
import com.demo.productservice.dto.ProductRequestDto;
import com.demo.productservice.dto.ProductResponseDto;
import com.demo.productservice.model.Product;
import com.demo.productservice.service.ProductService;
import com.demo.productservice.service.UserService;
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

    // test
    @GetMapping("/aaa/{text}")
    public String test(@PathVariable String text) {
        System.out.println("여기는 상품 컨트롤러 !!");
        return productService.test(text);
    }

    // 상품 등록
    @PostMapping
    public ResponseEntity<ApiUtils.ApiResult<String>> addProduct(@RequestBody ProductRequestDto productRequestDto) {
        productService.saveProduct(productRequestDto);
        return ResponseEntity.ok(ApiUtils.success("상품 등록 완료!"));
    }


    // 상품 리스트 조회
    @GetMapping
    public ResponseEntity<ApiUtils.ApiResult<List<ProductResponseDto>>> productList() {
        List<ProductResponseDto> products = productService.getAllProducts()
                .stream()
                .map(ProductResponseDto::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiUtils.success(products));
    }

    // 상품 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiUtils.ApiResult<ProductResponseDto>> productDetail(@PathVariable("id") Long id) {
        Optional<Product> result = productService.findItemById(id);

        if (result.isPresent()) {
            ProductResponseDto productDto = new ProductResponseDto(result.get());
            return ResponseEntity.ok(ApiUtils.success(productDto));
        } else {
            return ResponseEntity.status(404).body(ApiUtils.error("해당 상품을 찾을 수 없습니다."));
        }
    }

}