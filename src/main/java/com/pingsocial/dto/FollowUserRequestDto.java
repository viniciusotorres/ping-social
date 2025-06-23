package com.pingsocial.dto;

import jakarta.validation.constraints.NotNull;

public record FollowUserRequestDto(
    Long followUserId
) {
}
