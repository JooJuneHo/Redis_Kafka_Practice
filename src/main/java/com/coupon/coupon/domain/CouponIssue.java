package com.coupon.coupon.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"couponId", "userId"}))
public class CouponIssue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long couponId;
    private Long userId;
    private LocalDateTime createdAt = LocalDateTime.now();

    public CouponIssue(Long couponId, Long userId) {
        this.couponId = couponId;
        this.userId = userId;
    }
}