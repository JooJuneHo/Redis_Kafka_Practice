package com.coupon.coupon.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class CouponRequestDto {
    private final String name;
    private final int totalQuantity;

    public CouponRequestDto(String name, int totalQuantity) {
        this.name = name;
        this.totalQuantity = totalQuantity;
    }
}
