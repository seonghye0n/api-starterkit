package com.example.apistarterkit.domain.member.entity;

import com.example.apistarterkit.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    // 소셜 로그인 전용 계정은 비밀번호가 없을 수 있어 nullable로 둔다.
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Builder
    private Member(String email, String password, String nickname, Role role, AuthProvider provider) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.provider = provider;
    }

    public static Member createLocal(String email, String encodedPassword, String nickname) {
        return Member.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .role(Role.USER)
                .provider(AuthProvider.LOCAL)
                .build();
    }

    public static Member createOAuth(String email, String nickname, AuthProvider provider) {
        return Member.builder()
                .email(email)
                .nickname(nickname)
                .role(Role.USER)
                .provider(provider)
                .build();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
