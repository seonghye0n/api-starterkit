package com.example.apistarterkit.domain.member.service;

import com.example.apistarterkit.domain.member.dto.response.MemberResponse;
import com.example.apistarterkit.domain.member.entity.Member;
import com.example.apistarterkit.domain.member.repository.MemberRepository;
import com.example.apistarterkit.global.exception.CustomException;
import com.example.apistarterkit.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberResponse getMyInfo(Long memberId) {
        return MemberResponse.from(getMember(memberId));
    }

    @Transactional
    public MemberResponse updateNickname(Long memberId, String nickname) {
        Member member = getMember(memberId);
        member.updateNickname(nickname);
        return MemberResponse.from(member);
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
