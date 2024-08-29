package com.demo.productservice.service;

import com.demo.productservice.dto.ProductRequestDto;
import com.demo.productservice.model.Product;
import com.demo.productservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final RedisTemplate<String, String> redisTemplate;

    public ProductService(ProductRepository productRepository, RedisTemplate<String, String> redisTemplate) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
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

    /**
     * Redis에서 임시 재고 수량을 조회
     * 레디스에 없을 땐 db에서 조회
     * @param productId 상품 ID
     * @return 임시 재고 수량
     */
    public int getStockFromRedis(Long productId) {
        // Redis에서 재고 조회
        String stockStr = redisTemplate.opsForValue().get("stock:" + productId);
        if (stockStr != null) {
            try {
                return Integer.parseInt(stockStr);
            } catch (NumberFormatException e) {
                // 로그를 남기거나 예외 처리 (선택 사항)
                return 0;  // 기본값으로 반환 또는 예외 발생
            }
        }
        // Redis에 재고 정보가 없는 경우 DB에서 조회
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            // 데이터베이스에서 조회한 재고를 Redis에 캐싱
            redisTemplate.opsForValue().set("stock:" + productId, String.valueOf(product.getStock()));
            return product.getStock();
        }
        return 0;  // 제품이 없는 경우 기본값 반환 또는 예외 처리
    }


//    // 재고 업데이트 -> Product 객체를 직접 저장
//    public void updateStock(Product product) {
//        productRepository.save(product);
//    }
//
//    // Redis에 재고 수량 업데이트
//    public void updateStockInRedis(Long productId, int stock) {
//        String key = "product:stock:" + productId;
//        redisTemplate.opsForValue().set(key, stock);  // Integer로 저장
//    }
}
