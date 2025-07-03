package com.pingsocial.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record ResponsePost(
        Long id,
        String authorNickname,
        Set<Long> tribeIds,
        String content,
        LocalDateTime createdAt
) {}