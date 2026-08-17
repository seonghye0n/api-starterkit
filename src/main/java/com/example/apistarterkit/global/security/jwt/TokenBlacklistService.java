package com.example.apistarterkit.global.security.jwt;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 로그아웃된 Access Token을 남은 유효시간만큼만 Redis에 저장해 즉시 무효화한다.
 * TTL을 남은 유효시간으로 제한해 Redis 메모리가 무한히 늘어나지 않도록 한다.
 */
@Component
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "blacklist:";

    private final StringRedisTemplate redisTemplate;

    public void blacklist(String accessToken, long remainingValidityMillis) {
        if (remainingValidityMillis <= 0) {
            return;
        }
        redisTemplate.opsForValue().set(KEY_PREFIX + accessToken, "logout", remainingValidityMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + accessToken));
    }
}
