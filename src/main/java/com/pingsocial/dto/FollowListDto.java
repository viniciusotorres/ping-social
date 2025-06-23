package com.pingsocial.dto;

public record FollowListDto(
        Long followedUserId,
        String followedUserEmail
) {
}
