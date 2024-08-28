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

    @Scheduled(fixedRate = 60000)  // test - > 1분마다 실행
    public void checkForTimeoutOrders() {
        log.info("Checking for timeout orders...");

        Set<Object> orderKeys = redisTemplate.opsForHash().keys("orders");
        if (orderKeys != null) {
            for (Object orderKeyObj : orderKeys) {
                String orderKey = orderKeyObj.toString();

                Object orderObject = redisTemplate.opsForHash().get("orders", orderKey);
                if (orderObject == null) {
                    continue;
                }

                // LinkedHashMap을 PrepareOrderDto로 변환
                PrepareOrderDto orderDto = objectMapper.convertValue(orderObject, PrepareOrderDto.class);

                // 이미 TIME_OUT 상태이면 로그를 남기지 않고 스킵
                if (OrderStatus.TIME_OUT.equals(orderDto.getStatus())) {
                    continue;
                }

                Long ttl = redisTemplate.getExpire("orders:" + orderKey, TimeUnit.SECONDS);
                log.info("Order key {} has TTL: {}", orderKey, ttl);

                if (ttl != null && ttl <= 0) {
                    // 상태를 TIME_OUT으로 변경
                    orderDto.setStatus(OrderStatus.TIME_OUT);
                    redisTemplate.opsForHash().put("orders", orderKey, orderDto);
                    log.info("Order key {} has been set to TIME_OUT", orderKey);
                }
            }
        }
    }

}
