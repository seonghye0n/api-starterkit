package com.example.apistarterkit.domain.member.controller;

import com.example.apistarterkit.domain.member.dto.request.LoginRequest;
import com.example.apistarterkit.domain.member.dto.request.ReissueRequest;
import com.example.apistarterkit.domain.member.dto.request.SignUpRequest;
import com.example.apistarterkit.domain.member.dto.response.MemberResponse;
import com.example.apistarterkit.domain.member.dto.response.TokenResponse;
import com.example.apistarterkit.domain.member.service.AuthService;
import com.example.apistarterkit.global.response.ApiResponse;
import com.example.apistarterkit.global.security.SecurityUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "회원가입/로그인/토큰 재발급/로그아웃")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<MemberResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(authService.signUp(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(@Valid @RequestBody ReissueRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.reissue(request.refreshToken())));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authorizationHeader) {
        String accessToken = authorizationHeader.replaceFirst("Bearer ", "");
        authService.logout(SecurityUtil.getCurrentMemberId(), accessToken);
        return ResponseEntity.ok(ApiResponse.empty());
    }
}
