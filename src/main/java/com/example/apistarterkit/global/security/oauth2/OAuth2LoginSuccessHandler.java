package com.example.apistarterkit.global.security.oauth2;

import com.example.apistarterkit.domain.member.repository.RefreshTokenRepository;
import com.example.apistarterkit.global.security.jwt.JwtTokenProvider;
import com.example.apistarterkit.global.security.jwt.TokenPair;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * OAuth2 로그인 성공 시 세션을 만드는 대신 자체 JWT를 발급해 프론트엔드로 리다이렉트한다.
 * 데모 목적으로 토큰을 쿼리 파라미터에 담아 전달한다.
 * 운영 환경에서는 refreshToken을 httpOnly Secure 쿠키로 내려주는 방식을 권장한다.
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        TokenPair tokens = jwtTokenProvider.generateTokenPair(oAuth2User.getMemberId(), oAuth2User.getRole());
        refreshTokenRepository.save(oAuth2User.getMemberId(), tokens.refreshToken(),
                jwtTokenProvider.getRefreshTokenValidityMillis());

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", tokens.accessToken())
                .queryParam("refreshToken", tokens.refreshToken())
                .build().toUriString();

        response.sendRedirect(targetUrl);
    }
}
