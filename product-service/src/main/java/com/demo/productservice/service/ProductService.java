package com.demo.productservice.service;

import com.demo.productservice.dto.ProductRequestDto;
import com.demo.productservice.model.Product;
import com.demo.productservice.repository.ProductRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final RedisTemplate<String, Integer> redisTemplate;
    public ProductService(ProductRepository productRepository, RedisTemplate<String, Integer> redisTemplate) {
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

    // 재고 업데이트 -> Product 객체를 직접 저장
    public void updateStock(Product product) {
        productRepository.save(product);
    }

//    // Redis에서 재고 수량 조회
//    public int getStockFromRedis(Long productId) {
//        String key = "product:stock:" + productId;
//        Integer stock = redisTemplate.opsForValue().get(key);  // Integer로 명시적 캐스팅
//        if (stock != null) {
//            return stock;
//        }
//        // Redis에 없으면 DB에서 조회
//        Optional<Product> product = productRepository.findById(productId);
//        if (product.isPresent()) {
//            int dbStock = product.get().getStock();
//            redisTemplate.opsForValue().set(key, dbStock);
//            return dbStock;
//        }
//        return 0; // or throw exception
//    }

    // Redis에 재고 수량 업데이트
    public void updateStockInRedis(Long productId, int stock) {
        String key = "product:stock:" + productId;
        redisTemplate.opsForValue().set(key, stock);  // Integer로 저장
    }
}
