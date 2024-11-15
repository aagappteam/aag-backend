package aagapp_backend.services;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String userId, String action) {
        String key = userId + ":" + action;
        return buckets.computeIfAbsent(key, this::createNewBucket);
    }

    private Bucket createNewBucket(String key) {
        Bandwidth limit;
        String action = key.split(":")[1];

        if (action.startsWith("/otp/send-otp")) {
            limit = Bandwidth.classic(1, Refill.greedy(1, Duration.ofSeconds(30)));
        } else if (action.startsWith("/api/rate-limit")) {
            limit = Bandwidth.classic(2, Refill.greedy(2, Duration.ofMinutes(1)));
        } else {
            limit = Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1)));
        }
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }
}
