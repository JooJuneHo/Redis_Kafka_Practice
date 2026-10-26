package com.coupon.coupon.consumer;

import com.coupon.coupon.domain.CouponIssue;
import com.coupon.coupon.repository.CouponIssueRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponIssueConsumer {
    private final CouponIssueRepository couponIssueRepository;

    @KafkaListener(topics = "coupon-issued", autoStartup = "${consumer.enabled:true}")
    public void consume(ConsumerRecord<String, String> record) {
        Long couponId = Long.valueOf(record.key());
        Long userId = Long.valueOf(record.value());
        couponIssueRepository.save(new CouponIssue(couponId, userId));
    }
}
