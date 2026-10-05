package com.karan.risk.paymentriskengine.rules.velocity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Redis-backed velocity counter using sorted sets.
 *
 * Design:
 *   - Key format: velocity:sender:{senderId}
 *   - Each transaction adds a member with score = epoch millis
 *   - Member value = unique transaction ID (avoid collisions for same ms)
 *   - ZREMRANGEBYSCORE evicts entries older than window
 *   - ZCARD returns current count
 *   - EXPIRE ensures cleanup even if sender goes inactive
 *
 * Operations are O(log N) for ZADD and ZREMRANGEBYSCORE, O(1) for ZCARD.
 * Works across all app instances — shared state in Redis.
 */
@Component
public class RedisVelocityCounter {

    private static final Logger log = LoggerFactory.getLogger(RedisVelocityCounter.class);

    private static final String KEY_PREFIX = "velocity:sender:";

    private final StringRedisTemplate redis;

    public RedisVelocityCounter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /**
     * Record a transaction and return the current count within the window.
     *
     * @param senderId   sender identifier
     * @param window     sliding window duration (e.g., 10 minutes)
     * @return number of transactions in the window (including this one)
     */
    public int recordAndCount(String senderId, Duration window) {
        String key = KEY_PREFIX + senderId;
        Instant now = Instant.now();
        long nowMillis = now.toEpochMilli();
        long cutoffMillis = now.minus(window).toEpochMilli();

        try {
            ZSetOperations<String, String> ops = redis.opsForZSet();

            // 1. Evict entries older than the window
            ops.removeRangeByScore(key, 0, cutoffMillis);

            // 2. Add current transaction (unique member to avoid collision in same ms)
            String member = nowMillis + ":" + UUID.randomUUID();
            ops.add(key, member, nowMillis);

            // 3. Auto-expire the whole key if no activity for window * 2
            redis.expire(key, window.multipliedBy(2));

            // 4. Return current count
            Long count = ops.zCard(key);
            int result = count == null ? 0 : count.intValue();

            log.debug("Velocity count for sender={} is {}", senderId, result);
            return result;

        } catch (Exception ex) {
            // Fail-safe: Redis down should not crash the payment flow.
            // Return 0 so rule treats it as pass.
            log.error("Redis velocity counter failed for sender={}; treating as 0", senderId, ex);
            return 0;
        }
    }

    /**
     * Get current count without recording. Useful for diagnostics.
     */
    public int currentCount(String senderId, Duration window) {
        String key = KEY_PREFIX + senderId;
        long cutoffMillis = Instant.now().minus(window).toEpochMilli();

        try {
            ZSetOperations<String, String> ops = redis.opsForZSet();
            Set<String> members = ops.rangeByScore(key, cutoffMillis, Double.MAX_VALUE);
            return members == null ? 0 : members.size();
        } catch (Exception ex) {
            log.error("Redis currentCount failed for sender={}", senderId, ex);
            return 0;
        }
    }
}
