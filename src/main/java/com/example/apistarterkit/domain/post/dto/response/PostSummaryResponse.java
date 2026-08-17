package com.example.apistarterkit.domain.post.dto.response;

import com.example.apistarterkit.domain.post.entity.Post;
import java.time.LocalDateTime;

public record PostSummaryResponse(
        Long id,
        String title,
        String authorNickname,
        LocalDateTime createdAt
) {
    public static PostSummaryResponse from(Post post) {
        return new PostSummaryResponse(
                post.getId(), post.getTitle(), post.getMember().getNickname(), post.getCreatedAt());
    }
}
