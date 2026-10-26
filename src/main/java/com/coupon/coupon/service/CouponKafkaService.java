package com.coupon.coupon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponKafkaService {
    private final CouponRedisService couponRedisService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void issue(Long couponId, Long userId) {
        couponRedisService.check(couponId, userId);                                     // Redis 검사
        kafkaTemplate.send("coupon-issued", couponId.toString(), userId.toString()).join(); // DB 대신 Kafka로
    }
}
