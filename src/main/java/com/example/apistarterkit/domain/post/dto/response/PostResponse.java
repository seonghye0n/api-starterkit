package com.example.apistarterkit.domain.post.dto.response;

import com.example.apistarterkit.domain.post.entity.Post;
import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String content,
        String authorNickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(), post.getTitle(), post.getContent(),
                post.getMember().getNickname(), post.getCreatedAt(), post.getUpdatedAt());
    }
}
