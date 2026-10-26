package com.coupon.coupon.controller;

import com.coupon.coupon.dto.CouponRequestDto;
import com.coupon.coupon.service.CouponKafkaService;
import com.coupon.coupon.service.CouponLockService;
import com.coupon.coupon.service.CouponRedisService;
import com.coupon.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;
    private final CouponLockService couponLockService;
    private final CouponRedisService couponRedisService;
    private final CouponKafkaService couponKafkaService;

    @PostMapping
    public Long create(@RequestBody CouponRequestDto couponRequestDto) {
        return couponService.create(couponRequestDto.getName(), couponRequestDto.getTotalQuantity());
    }

    @PostMapping("/{id}/issue")
    public void issue(@PathVariable Long id, @RequestParam Long userId) {
        couponService.issue(id, userId);
    }

    @PostMapping("/{id}/issue/lock")
    public void issueWithLock(@PathVariable Long id, @RequestParam Long userId) {
        couponLockService.issue(id, userId);
    }

    @PostMapping("/{id}/issue/redis")
    public void issueWithRedis(@PathVariable Long id, @RequestParam Long userId) {
        couponRedisService.issue(id, userId);
    }

    @PostMapping("/{id}/issue/kafka")
    public void issueWithKafka(@PathVariable Long id, @RequestParam Long userId) {
        couponKafkaService.issue(id, userId);
    }
}
