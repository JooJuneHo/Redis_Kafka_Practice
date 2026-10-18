package com.coupon.coupon.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CouponLockService {
    private final RedissonClient redissonClient;
    private final CouponService couponService;

    public void issue(Long couponId, Long userId) {
        RLock lock = redissonClient.getLock("lock:coupon:" + couponId);
        try {
            // 최대 30초 기다리고, 락을 잡으면 3초 뒤 자동 해제 (서버가 죽어도 락이 영원히 남지 않게)
            if (!lock.tryLock(30, 3, TimeUnit.SECONDS)) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "락 획득 실패");
            }
            couponService.issue(couponId, userId);   // 트랜잭션 시작 → 커밋까지 여기서 끝남
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();                        // 커밋이 끝난 뒤에 락 해제
            }
        }
    }
}