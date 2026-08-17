package com.example.apistarterkit.global.security.jwt;

import com.example.apistarterkit.domain.member.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * JWT 발급/파싱/검증을 담당한다. Access Token은 자기 완결적(self-contained)이므로
 * 요청마다 DB를 조회하지 않고 토큰에 담긴 memberId/role만으로 인증 정보를 구성한다.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private static final String CLAIM_ROLE = "role";

    private final SecretKey key;
    private final long accessTokenValidityMillis;
    private final long refreshTokenValidityMillis;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity}") long accessTokenValidityMillis,
            @Value("${jwt.refresh-token-validity}") long refreshTokenValidityMillis
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityMillis = accessTokenValidityMillis;
        this.refreshTokenValidityMillis = refreshTokenValidityMillis;
    }

    public TokenPair generateTokenPair(Long memberId, Role role) {
        return new TokenPair(createAccessToken(memberId, role), createRefreshToken(memberId));
    }

    public String createAccessToken(Long memberId, Role role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(CLAIM_ROLE, role.name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenValidityMillis))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String createRefreshToken(Long memberId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenValidityMillis))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public long getRefreshTokenValidityMillis() {
        return refreshTokenValidityMillis;
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("만료된 JWT 토큰입니다.");
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("유효하지 않은 JWT 토큰입니다.");
        }
        return false;
    }

    public Long getMemberId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public long getRemainingValidity(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        Long memberId = Long.valueOf(claims.getSubject());
        String role = claims.get(CLAIM_ROLE, String.class);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        return new UsernamePasswordAuthenticationToken(memberId, token, authorities);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
