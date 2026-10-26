package com.coupon.coupon.service;

import com.coupon.coupon.domain.Coupon;
import com.coupon.coupon.domain.CouponIssue;
import com.coupon.coupon.repository.CouponIssueRepository;
import com.coupon.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CouponRedisService {
    private final StringRedisTemplate redis;
    private final CouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;

//    public void issue(Long couponId, Long userId) {
//        Coupon coupon = couponRepository.findById(couponId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
//
//        // 1. 중복 확인: Set에 새로 들어가면 1, 이미 있으면 0
//        Long added = redis.opsForSet().add("coupon:" + couponId + ":users", userId.toString());
//        if (added == null || added == 0) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 발급받음");
//        }
//
//        // 2. 번호표 뽑기: 1, 2, 3, ... 각 요청이 서로 다른 숫자를 받음
//        Long count = redis.opsForValue().increment("coupon:" + couponId + ":count");
//        if (count > coupon.getTotalQuantity()) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "수량 소진");
//        }
//
//        // 3. 당첨자만 DB 저장 (issuedQuantity는 건드리지 않음)
//        couponIssueRepository.save(new CouponIssue(couponId, userId));
//    }

    public void issue(Long couponId, Long userId) {
        check(couponId, userId);
        couponIssueRepository.save(new CouponIssue(couponId, userId));
    }

    // SADD + INCR 검사: 통과 못 하면 409 예외
    public void check(Long couponId, Long userId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Long added = redis.opsForSet().add("coupon:" + couponId + ":users", userId.toString());
        if (added == null || added == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 발급받음");
        }
        Long count = redis.opsForValue().increment("coupon:" + couponId + ":count");
        if (count > coupon.getTotalQuantity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "수량 소진");
        }
    }
}