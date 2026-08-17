package com.example.apistarterkit.domain.member.repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Refresh Token을 Redis에 저장/조회/삭제한다. JPA가 아닌 Redis를 저장소로 쓰므로
 * memberId 기준 TTL(=refresh token 만료시간)이 지나면 자동으로 정리된다.
 */
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;

    public void save(Long memberId, String refreshToken, long validityMillis) {
        redisTemplate.opsForValue().set(KEY_PREFIX + memberId, refreshToken, validityMillis, TimeUnit.MILLISECONDS);
    }

    public Optional<String> findByMemberId(Long memberId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_PREFIX + memberId));
    }

    public void deleteByMemberId(Long memberId) {
        redisTemplate.delete(KEY_PREFIX + memberId);
    }
}
