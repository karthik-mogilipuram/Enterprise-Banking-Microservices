package com.enterprise.banking.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceCacheService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String BALANCE_KEY_PREFIX = "account:balance:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    public void cacheBalance(UUID accountId, BigDecimal balance) {
        String key = BALANCE_KEY_PREFIX + accountId;
        try {
            redisTemplate.opsForValue().set(key, balance.toString(), CACHE_TTL);
            log.debug("Cached balance for account: {}", accountId);
        } catch (Exception e) {
            log.warn("Failed to cache balance for account: {}", accountId, e);
        }
    }

    public Optional<BigDecimal> getCachedBalance(UUID accountId) {
        String key = BALANCE_KEY_PREFIX + accountId;
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("Retrieved cached balance for account: {}", accountId);
                return Optional.of(new BigDecimal(value));
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve cached balance for account: {}", accountId, e);
        }
        return Optional.empty();
    }

    public void evictBalance(UUID accountId) {
        String key = BALANCE_KEY_PREFIX + accountId;
        try {
            redisTemplate.delete(key);
            log.debug("Evicted balance cache for account: {}", accountId);
        } catch (Exception e) {
            log.warn("Failed to evict balance cache for account: {}", accountId, e);
        }
    }
}