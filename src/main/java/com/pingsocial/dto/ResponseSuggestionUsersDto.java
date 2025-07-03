package com.pingsocial.dto;

import java.util.List;

public record ResponseSuggestionUsersDto(
        Long id,
        String nickname,
        String avatarInitials,
        int countFollowers,
        List<String> namesTribes,
        boolean isFollowing,
        double distanceFromUser
) {
}
