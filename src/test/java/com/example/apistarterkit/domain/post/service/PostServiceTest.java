package com.example.apistarterkit.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.example.apistarterkit.domain.member.entity.Member;
import com.example.apistarterkit.domain.member.repository.MemberRepository;
import com.example.apistarterkit.domain.post.dto.request.PostCreateRequest;
import com.example.apistarterkit.domain.post.dto.request.PostUpdateRequest;
import com.example.apistarterkit.domain.post.dto.response.PostResponse;
import com.example.apistarterkit.domain.post.entity.Post;
import com.example.apistarterkit.domain.post.repository.PostRepository;
import com.example.apistarterkit.global.exception.CustomException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("작성자가 아니면 게시글을 수정할 수 없다")
    void updateFailsWhenNotOwner() {
        Member writer = Member.createLocal("writer@example.com", "encoded", "writer");
        ReflectionTestUtils.setField(writer, "id", 1L);
        Post post = Post.create("title", "content", writer);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.update(2L, 1L, new PostUpdateRequest("new title", "new content")))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("게시글을 생성하면 작성자 닉네임이 포함된 응답을 반환한다")
    void create() {
        Member writer = Member.createLocal("writer@example.com", "encoded", "writer");
        given(memberRepository.findById(1L)).willReturn(Optional.of(writer));
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        PostResponse response = postService.create(1L, new PostCreateRequest("title", "content"));

        assertThat(response.title()).isEqualTo("title");
        assertThat(response.authorNickname()).isEqualTo("writer");
    }
}
