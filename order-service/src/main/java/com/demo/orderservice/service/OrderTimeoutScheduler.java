package com.demo.orderservice.service;


import com.demo.orderservice.dto.PrepareOrderDto;
import com.demo.orderservice.model.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OrderTimeoutScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private ObjectMapper objectMapper;

    public OrderTimeoutScheduler(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedRate = 60000) // 매 1분마다 실행
    public void checkAndExpireOrders() {
        log.info("Order timeout check started.");

        Set<Object> orderKeys = redisTemplate.opsForHash().keys("orders");

        if (orderKeys != null) {
            for (Object orderKeyObj : orderKeys) {
                String orderKey = orderKeyObj.toString();
                // 남은 TTL 확인
                Long ttl = redisTemplate.getExpire(orderKey, TimeUnit.SECONDS);
                if (ttl != null && ttl <= 0) {
                    log.info("Order key {} has expired TTL.", orderKey);

                    // 주문 객체 조회
                    Object orderObject = redisTemplate.opsForHash().get("orders", orderKey);
                    if (orderObject != null) {
                        PrepareOrderDto orderDto = objectMapper.convertValue(orderObject, PrepareOrderDto.class);

                        // 상태를 TIME_OUT으로 업데이트
                        orderDto.setStatus(OrderStatus.TIME_OUT);
                        log.info("Order {} is set to TIME_OUT status.", orderDto.getOrderId());

                        // 갱신된 주문 객체를 다시 Redis에 저장
                        redisTemplate.opsForHash().put("orders", orderKey, orderDto);
                        log.info("Order {} has been updated to TIME_OUT status in Redis.", orderDto.getOrderId());
                    } else {
                        log.warn("Order object not found for key: {}", orderKey);
                    }
                } else {
                    log.info("Order key {} is still active with TTL: {}", orderKey, ttl);
                }
            }
        } else {
            log.info("No orders found in Redis.");
        }

        log.info("Order timeout check completed.");
    }

}
