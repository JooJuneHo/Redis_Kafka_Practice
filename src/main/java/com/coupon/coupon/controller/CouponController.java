package com.coupon.coupon.controller;

import com.coupon.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    public record CreateRequest(String name, int totalQuantity) {}

    @PostMapping
    public Long create(@RequestBody CreateRequest req) {
        return couponService.create(req.name, req.totalQuantity());
    }

    @PostMapping("/{id}/issue")
    public void issue(@PathVariable Long id, @RequestParam Long userId) {
        couponService.issue(id, userId);
    }
}
