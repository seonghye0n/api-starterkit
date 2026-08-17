package com.example.apistarterkit.domain.post.controller;

import com.example.apistarterkit.domain.post.dto.request.PostCreateRequest;
import com.example.apistarterkit.domain.post.dto.request.PostUpdateRequest;
import com.example.apistarterkit.domain.post.dto.response.PostResponse;
import com.example.apistarterkit.domain.post.dto.response.PostSummaryResponse;
import com.example.apistarterkit.domain.post.service.PostService;
import com.example.apistarterkit.global.response.ApiResponse;
import com.example.apistarterkit.global.response.PageResponse;
import com.example.apistarterkit.global.security.SecurityUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Post", description = "게시글 CRUD 및 페이징 조회")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> create(@Valid @RequestBody PostCreateRequest request) {
        PostResponse response = postService.create(SecurityUtil.getCurrentMemberId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ApiResponse<PageResponse<PostSummaryResponse>> getList(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(postService.getList(pageable));
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getDetail(@PathVariable Long postId) {
        return ApiResponse.success(postService.getDetail(postId));
    }

    @PutMapping("/{postId}")
    public ApiResponse<PostResponse> update(@PathVariable Long postId, @Valid @RequestBody PostUpdateRequest request) {
        return ApiResponse.success(postService.update(SecurityUtil.getCurrentMemberId(), postId, request));
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> delete(@PathVariable Long postId) {
        postService.delete(SecurityUtil.getCurrentMemberId(), postId);
        return ApiResponse.empty();
    }
}
