package com.example.apistarterkit.domain.post.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.apistarterkit.domain.member.entity.Member;
import com.example.apistarterkit.domain.member.repository.MemberRepository;
import com.example.apistarterkit.domain.post.entity.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("게시글 목록을 페이징하여 조회한다")
    void findAllWithPaging() {
        Member member = memberRepository.save(Member.createLocal("test@example.com", "encoded", "tester"));
        for (int i = 0; i < 15; i++) {
            postRepository.save(Post.create("title" + i, "content" + i, member));
        }

        Page<Post> page = postRepository.findAll(PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(15);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }
}
