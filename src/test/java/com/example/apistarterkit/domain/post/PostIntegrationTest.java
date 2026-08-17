package com.example.apistarterkit.domain.post;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.apistarterkit.domain.member.dto.request.LoginRequest;
import com.example.apistarterkit.domain.member.dto.request.SignUpRequest;
import com.example.apistarterkit.domain.member.dto.response.MemberResponse;
import com.example.apistarterkit.domain.member.dto.response.TokenResponse;
import com.example.apistarterkit.domain.member.service.AuthService;
import com.example.apistarterkit.domain.post.dto.request.PostCreateRequest;
import com.example.apistarterkit.domain.post.dto.response.PostResponse;
import com.example.apistarterkit.domain.post.dto.response.PostSummaryResponse;
import com.example.apistarterkit.domain.post.service.PostService;
import com.example.apistarterkit.global.response.PageResponse;
import com.example.apistarterkit.support.AbstractContainerBaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 실제 MySQL/Redis 컨테이너(Testcontainers)까지 띄워 회원가입 -> 로그인 -> 글쓰기 -> 목록 조회 흐름을 검증한다.
 * Docker가 필요하므로 로컬/CI에 Docker 데몬이 떠 있어야 통과한다.
 */
@ActiveProfiles("test")
@SpringBootTest
class PostIntegrationTest extends AbstractContainerBaseTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private PostService postService;

    @Test
    @DisplayName("회원가입 후 로그인한 회원이 게시글을 작성하고 목록에서 조회할 수 있다")
    void signUpLoginCreatePostAndList() {
        MemberResponse member = authService.signUp(
                new SignUpRequest("integration@example.com", "password123", "tester"));

        TokenResponse tokens = authService.login(new LoginRequest("integration@example.com", "password123"));
        assertThat(tokens.accessToken()).isNotBlank();

        PostResponse created = postService.create(member.id(), new PostCreateRequest("첫 글", "내용입니다"));
        assertThat(created.title()).isEqualTo("첫 글");

        PageResponse<PostSummaryResponse> page = postService.getList(PageRequest.of(0, 10));
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.content().get(0).title()).isEqualTo("첫 글");
    }
}
