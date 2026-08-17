package com.example.apistarterkit.global.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.apistarterkit.domain.member.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
            "test-secret-key-for-junit-tests-must-be-at-least-32-bytes-long",
            1000L * 60 * 30,
            1000L * 60 * 60 * 24 * 14
    );

    @Test
    @DisplayName("발급한 Access Token에서 memberId를 다시 꺼낼 수 있다")
    void createAndParseAccessToken() {
        String token = jwtTokenProvider.createAccessToken(1L, Role.USER);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getMemberId(token)).isEqualTo(1L);
    }

    @Test
    @DisplayName("만료된 토큰은 유효하지 않다")
    void expiredTokenIsInvalid() throws InterruptedException {
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(
                "test-secret-key-for-junit-tests-must-be-at-least-32-bytes-long", 1L, 1L);
        String token = shortLivedProvider.createAccessToken(1L, Role.USER);

        Thread.sleep(10);

        assertThat(shortLivedProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("변조된 토큰은 유효하지 않다")
    void tamperedTokenIsInvalid() {
        String token = jwtTokenProvider.createAccessToken(1L, Role.USER);

        assertThat(jwtTokenProvider.validateToken(token + "tampered")).isFalse();
    }
}
