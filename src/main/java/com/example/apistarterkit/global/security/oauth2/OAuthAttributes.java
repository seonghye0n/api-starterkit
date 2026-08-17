package com.example.apistarterkit.global.security.oauth2;

import com.example.apistarterkit.domain.member.entity.AuthProvider;
import com.example.apistarterkit.domain.member.entity.Member;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/**
 * Provider(Google 등)마다 사용자 정보 응답 형식이 다르므로, 이 클래스가 그 차이를 흡수해
 * 우리 서비스의 공통 형태(email, nickname, provider)로 변환한다.
 */
@Getter
public class OAuthAttributes {

    private final Map<String, Object> attributes;
    private final String nameAttributeKey;
    private final String email;
    private final String nickname;
    private final AuthProvider provider;

    @Builder
    private OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String email,
                             String nickname, AuthProvider provider) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.email = email;
        this.nickname = nickname;
        this.provider = provider;
    }

    public static OAuthAttributes of(String registrationId, String userNameAttributeName,
                                      Map<String, Object> attributes) {
        // 현재는 Google만 지원한다. 다른 provider를 추가하려면 registrationId로 분기 처리를 확장한다.
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .email((String) attributes.get("email"))
                .nickname((String) attributes.get("name"))
                .provider(AuthProvider.GOOGLE)
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    public Member toEntity() {
        return Member.createOAuth(email, nickname, provider);
    }
}
