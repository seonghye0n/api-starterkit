package com.example.apistarterkit.global.security;

import com.example.apistarterkit.global.exception.CustomException;
import com.example.apistarterkit.global.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    /**
     * JwtAuthenticationFilter가 principal로 memberId(Long)를 직접 넣어주므로 그대로 꺼내 쓴다.
     */
    public static Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Long memberId)) {
            throw new CustomException(ErrorCode.UNAUTHENTICATED);
        }
        return memberId;
    }
}
