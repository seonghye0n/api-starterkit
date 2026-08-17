package com.example.apistarterkit.domain.member.dto.response;

import com.example.apistarterkit.global.security.jwt.TokenPair;

public record TokenResponse(String accessToken, String refreshToken) {
    public static TokenResponse from(TokenPair tokenPair) {
        return new TokenResponse(tokenPair.accessToken(), tokenPair.refreshToken());
    }
}
