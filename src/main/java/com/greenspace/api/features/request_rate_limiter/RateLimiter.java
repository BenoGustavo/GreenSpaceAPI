package com.greenspace.api.features.request_rate_limiter;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

@Component
public class RateLimiter {
    private final Bucket bucket;

    public RateLimiter(
            @Value("${ratelimiter.capacity}") int capacity,
            @Value("${ratelimiter.refillTokens}") int refillTokens,
            @Value("${ratelimiter.refillPeriod}") int refillPeriod) {

        Bandwidth limit = Bandwidth.classic(capacity,
                Refill.intervally(refillTokens, Duration.ofMinutes(refillPeriod)));

        this.bucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public boolean isRateLimited() {
        boolean allowed = bucket.tryConsume(1);
        return !allowed;
    }
}
