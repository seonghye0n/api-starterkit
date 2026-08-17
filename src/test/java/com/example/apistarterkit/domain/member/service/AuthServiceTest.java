package com.example.apistarterkit.domain.member.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.example.apistarterkit.domain.member.dto.request.SignUpRequest;
import com.example.apistarterkit.domain.member.repository.MemberRepository;
import com.example.apistarterkit.domain.member.repository.RefreshTokenRepository;
import com.example.apistarterkit.global.exception.CustomException;
import com.example.apistarterkit.global.security.jwt.JwtTokenProvider;
import com.example.apistarterkit.global.security.jwt.TokenBlacklistService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("이미 가입된 이메일로는 회원가입할 수 없다")
    void signUpFailsWhenEmailDuplicated() {
        given(memberRepository.existsByEmail("dup@example.com")).willReturn(true);

        assertThatThrownBy(() -> authService.signUp(
                new SignUpRequest("dup@example.com", "password123", "nick")))
                .isInstanceOf(CustomException.class);
    }
}
