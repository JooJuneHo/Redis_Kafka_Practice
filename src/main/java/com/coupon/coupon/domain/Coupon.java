package com.coupon.coupon.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int totalQuantity;
    private int issuedQuantity;

    public Coupon(String name, int totalQuantity) {
        this.name = name;
        this.totalQuantity = totalQuantity;
    }

    public void issue() {
        if (issuedQuantity >= totalQuantity) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "수량 소진");
        }
        issuedQuantity++;
    }
}
