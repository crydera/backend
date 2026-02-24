package com.crydera.payment.service;

import com.crydera.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatusCacheService {

    private static final Duration TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redis;

    public void put(Payment.Kind kind, UUID paymentId, Payment.Status status) {
        redis.opsForValue().set(key(kind, paymentId), status.name(), TTL);
    }

    public Optional<Payment.Status> get(Payment.Kind kind, UUID paymentId) {
        String raw = redis.opsForValue().get(key(kind, paymentId));
        return Optional.ofNullable(raw).map(Payment.Status::valueOf);
    }

    private static String key(Payment.Kind kind, UUID id) {
        return kind.name().toLowerCase() + ":" + id + ":status";
    }
}
