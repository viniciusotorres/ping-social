package com.pingsocial.dto;

import java.time.LocalDateTime;
import java.util.Set;

public class CreatePostDto {
    private Long userId;
    private String content;
    private Set<Long> tribeIds;
    private String createdAt;

    public CreatePostDto() {
    }

    public CreatePostDto(Long userId, String content, Set<Long> tribeIds, String createdAt) {
        this.userId = userId;
        this.content = content;
        this.tribeIds = tribeIds;
        this.createdAt = createdAt;
    }



    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Set<Long> getTribeIds() {
        return tribeIds;
    }

    public void setTribeIds(Set<Long> tribeIds) {
        this.tribeIds = tribeIds;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime setCreatedAt() {
        return  LocalDateTime.now();
    }
}