package com.example.apistarterkit.domain.post.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.apistarterkit.domain.post.dto.request.PostCreateRequest;
import com.example.apistarterkit.domain.post.dto.response.PostResponse;
import com.example.apistarterkit.domain.post.service.PostService;
import com.example.apistarterkit.global.response.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @WebMvcTest는 컨트롤러 계층만 슬라이스로 로드하므로 SecurityConfig(빈으로 등록된 필터체인)는 적용되지 않는다.
 * SecurityUtil.getCurrentMemberId()가 읽는 SecurityContext를 테스트에서 직접 채워 인증 상태를 흉내낸다.
 */
@ActiveProfiles("test")
@WebMvcTest(
        controllers = PostController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostService postService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("게시글 목록을 페이징 조회하면 200을 반환한다")
    void getList() throws Exception {
        given(postService.getList(any(Pageable.class)))
                .willReturn(new PageResponse<>(List.of(), 0, 10, 0, 0, false));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("로그인한 사용자는 게시글을 작성할 수 있다")
    void create() throws Exception {
        authenticateAs(1L);
        given(postService.create(anyLong(), any(PostCreateRequest.class)))
                .willReturn(new PostResponse(1L, "title", "content", "tester", LocalDateTime.now(), LocalDateTime.now()));

        mockMvc.perform(post("/api/posts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new PostCreateRequest("title", "content"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("title"));
    }

    private void authenticateAs(Long memberId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(memberId, null, List.of()));
    }
}
