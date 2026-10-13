package com.coupon.coupon.service;

import com.coupon.coupon.domain.Coupon;
import com.coupon.coupon.domain.CouponIssue;
import com.coupon.coupon.repository.CouponIssueRepository;
import com.coupon.coupon.repository.CouponRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;

    private final CouponIssueRepository couponIssueRepository;

    @Transactional
    public Long create(String name, int totalQuantity) {
        return couponRepository.save(new Coupon(name, totalQuantity)).getId();
    }

    @Transactional
    public void issue(Long couponId, Long userId) {
        if (couponIssueRepository.existsByCouponIdAndUserId(couponId, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 발급받음");
        }
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        coupon.issue();                                         // issuedQuantity++
        couponIssueRepository.save(new CouponIssue(couponId, userId));
    }
}
