package com.example.apistarterkit.domain.post.service;

import com.example.apistarterkit.domain.member.entity.Member;
import com.example.apistarterkit.domain.member.repository.MemberRepository;
import com.example.apistarterkit.domain.post.dto.request.PostCreateRequest;
import com.example.apistarterkit.domain.post.dto.request.PostUpdateRequest;
import com.example.apistarterkit.domain.post.dto.response.PostResponse;
import com.example.apistarterkit.domain.post.dto.response.PostSummaryResponse;
import com.example.apistarterkit.domain.post.entity.Post;
import com.example.apistarterkit.domain.post.repository.PostRepository;
import com.example.apistarterkit.global.exception.CustomException;
import com.example.apistarterkit.global.exception.ErrorCode;
import com.example.apistarterkit.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public PostResponse create(Long memberId, PostCreateRequest request) {
        Member member = getMember(memberId);
        Post post = Post.create(request.title(), request.content(), member);
        return PostResponse.from(postRepository.save(post));
    }

    public PageResponse<PostSummaryResponse> getList(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);
        return PageResponse.from(posts, PostSummaryResponse::from);
    }

    public PostResponse getDetail(Long postId) {
        return PostResponse.from(getPost(postId));
    }

    @Transactional
    public PostResponse update(Long memberId, Long postId, PostUpdateRequest request) {
        Post post = getPost(postId);
        validateOwner(post, memberId);
        post.update(request.title(), request.content());
        return PostResponse.from(post);
    }

    @Transactional
    public void delete(Long memberId, Long postId) {
        Post post = getPost(postId);
        validateOwner(post, memberId);
        postRepository.delete(post);
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private void validateOwner(Post post, Long memberId) {
        if (!post.isWrittenBy(memberId)) {
            throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
        }
    }
}
