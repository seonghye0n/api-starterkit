package com.example.apistarterkit.domain.member.controller;

import com.example.apistarterkit.domain.member.dto.response.MemberResponse;
import com.example.apistarterkit.domain.member.service.MemberService;
import com.example.apistarterkit.global.response.ApiResponse;
import com.example.apistarterkit.global.security.SecurityUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "내 정보 조회/수정")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ApiResponse<MemberResponse> getMyInfo() {
        return ApiResponse.success(memberService.getMyInfo(SecurityUtil.getCurrentMemberId()));
    }

    @PatchMapping("/me/nickname")
    public ApiResponse<MemberResponse> updateNickname(@RequestParam @NotBlank String nickname) {
        return ApiResponse.success(memberService.updateNickname(SecurityUtil.getCurrentMemberId(), nickname));
    }
}
