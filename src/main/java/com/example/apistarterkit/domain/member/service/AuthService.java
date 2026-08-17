package com.example.apistarterkit.domain.member.service;

import com.example.apistarterkit.domain.member.dto.request.LoginRequest;
import com.example.apistarterkit.domain.member.dto.request.SignUpRequest;
import com.example.apistarterkit.domain.member.dto.response.MemberResponse;
import com.example.apistarterkit.domain.member.dto.response.TokenResponse;
import com.example.apistarterkit.domain.member.entity.Member;
import com.example.apistarterkit.domain.member.repository.MemberRepository;
import com.example.apistarterkit.domain.member.repository.RefreshTokenRepository;
import com.example.apistarterkit.global.exception.CustomException;
import com.example.apistarterkit.global.exception.ErrorCode;
import com.example.apistarterkit.global.security.jwt.JwtTokenProvider;
import com.example.apistarterkit.global.security.jwt.TokenBlacklistService;
import com.example.apistarterkit.global.security.jwt.TokenPair;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional
    public MemberResponse signUp(SignUpRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        Member member = Member.createLocal(
                request.email(), passwordEncoder.encode(request.password()), request.nickname());
        return MemberResponse.from(memberRepository.save(member));
    }

    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));

        if (member.getPassword() == null || !passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }

        return issueTokens(member);
    }

    public TokenResponse reissue(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Long memberId = jwtTokenProvider.getMemberId(refreshToken);
        String storedRefreshToken = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!storedRefreshToken.equals(refreshToken)) {
            // 저장된 값과 다르면 탈취/재사용 가능성이 있으므로 즉시 거부한다.
            throw new CustomException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return issueTokens(member);
    }

    public void logout(Long memberId, String accessToken) {
        refreshTokenRepository.deleteByMemberId(memberId);
        long remaining = jwtTokenProvider.getRemainingValidity(accessToken);
        tokenBlacklistService.blacklist(accessToken, remaining);
    }

    private TokenResponse issueTokens(Member member) {
        TokenPair tokens = jwtTokenProvider.generateTokenPair(member.getId(), member.getRole());
        refreshTokenRepository.save(member.getId(), tokens.refreshToken(), jwtTokenProvider.getRefreshTokenValidityMillis());
        return TokenResponse.from(tokens);
    }
}
