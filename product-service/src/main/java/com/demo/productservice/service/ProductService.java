package com.demo.productservice.service;

import com.demo.productservice.dto.ProductListResponseDto;
import com.demo.productservice.dto.ProductRequestDto;
import com.demo.productservice.dto.ProductResponseDto;
import com.demo.productservice.model.Product;
import com.demo.productservice.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;
    public ProductService(ProductRepository productRepository, RedisTemplate<String, String> redisTemplate,
                          RedissonClient redissonClient) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
    }

    // 상품 등록
    public void saveProduct(ProductRequestDto requestDto) {
        // dto에서 엔티티로 변환 후 저장
        Product product = requestDto.toProduct();
        productRepository.save(product);
    }

    // DTO 변환 포함 상품 리스트 조회
    public List<ProductListResponseDto> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(product -> new ProductListResponseDto(
                        product.getId(),
                        product.getTitle(),
                        product.getCategory(),
                        product.getPrice(),
                        product.getImageUrl()))
                .collect(Collectors.toList());
    }


    // 상품 상세 조회 -> 재고만 레디스 임시 재고로 보여줄 것 !! 
    public Optional<ProductResponseDto> findItemDetailById(Long id) {
        Optional<Product> productOpt = productRepository.findByIdWithLock(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            int remainingStock = getStockFromRedis(product.getId());
            ProductResponseDto productDto = new ProductResponseDto(product, remainingStock);
            return Optional.of(productDto);
        } else {
            return Optional.empty();
        }
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
        Product product = productRepository.findByIdWithLock(productId).orElse(null);
        if (product != null) {
            // 데이터베이스에서 조회한 재고를 Redis에 캐싱
            redisTemplate.opsForValue().set("stock:" + productId, String.valueOf(product.getStock()));
            return product.getStock();
        }
        return 0;  // 제품이 없는 경우 기본값 반환 또는 예외 처리
    }


    public void updateProductStock(Long productId, int newStock) {
        // 1. 상품 정보를 DB에서 조회
        Product product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + productId));

        // 2. 상품의 재고 업데이트
        product.setStock(newStock);
        productRepository.save(product);

        // todo : 레디스엔 이때 업데이트 해주면 안 되는 ??..
        /*
        ex)
        1~10번 사용자 / 상품 재고 10개 (1인 1개 구매)
        1번 유저 결제 완료 -> 오더에서 이 메서드 호출해서 상품 db에 9개로 업데이트
        -> 근데 아직 결제 완료 안 한 사람들 (그니까 주문 최종 완료 안 된 유저들)이 있는 상태면
        -> 레디스에 있는 임시 재고랑 상품 db에 있는 재고랑 불일치한 게 맞음
        -> 그래서 여기서 상품 db 재고 업데이트 될 때 레디스에도 같이 업데이트 해버리면 이상해짐 ...!
        -> 더 뒤에 온 사람들한테 갑자기 재고가 생겨나 보이는 것처럼 될 수 있는 ?!
        -> 일단 내가 생각한 게 맞는지 정답인지는 모르겠지만 일단 여기선 레디스 업데이트 해주지 말자 !
        * */
    }


    // 상품의 stock 수량을 조회하는 메서드
    @Transactional
    public int getProductStock(Long productId) {
        // DB에서 Product 엔티티를 찾고 stock 수량 반환
        Optional<Product> product = productRepository.findByIdWithLock(productId);
        if (product.isPresent()) {
            return product.get().getStock();  // Product 엔티티의 getStock() 메서드 호출
        } else {
            // 상품이 없을 경우 예외 처리
            return 0;  // 기본값 또는 예외를 발생시키도록 변경할 수 있음
        }
    }

    /*@Transactional
    public void checkAndDeductStock(Long productId, int quantityToOrder) {
        // 비관적 락을 걸고 상품을 조회
        Product product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));

        int currentStock = product.getStock();

        if (currentStock < quantityToOrder) {
            throw new RuntimeException("상품 재고가 부족합니다: " + productId);
        }
        // 재고 차감
        product.setStock(currentStock - quantityToOrder);
        productRepository.save(product);
    }*/

    @Transactional
    public void checkAndDeductStock(Long productId, int quantityToOrder) {
        RLock lock = redissonClient.getLock("stock_lock:" + productId);  // 락 생성

        try {
            // 락을 획득하려고 시도 (5초 동안 시도하고, 10초 동안 락이 유지됨)
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                try {
                    // 비관적 락을 걸고 상품을 조회
                    Product product = productRepository.findByIdWithLock(productId)
                            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));

                    int currentStock = product.getStock();

                    if (currentStock < quantityToOrder) {
                        throw new RuntimeException("상품 재고가 부족합니다: " + productId);
                    }

                    // 재고 차감
                    product.setStock(currentStock - quantityToOrder);
                    productRepository.save(product);

                    // Redis 캐시된 재고도 업데이트
                    redisTemplate.opsForValue().set("stock:" + productId, String.valueOf(currentStock - quantityToOrder));

                } finally {
                    lock.unlock();  // 락 해제
                }
            } else {
                throw new RuntimeException("재고 차감을 위한 락을 획득하지 못했습니다.");
            }
        } catch (InterruptedException e) {
            log.error("락 획득 중 인터럽트 발생", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("재고 차감 중 오류가 발생했습니다.", e);
        } catch (Exception e) {
            log.error("재고 차감 중 예외 발생", e);
            throw new RuntimeException("재고 차감 중 오류가 발생했습니다.", e);
        }
    }


}
