package com.pingsocial.dto;

import java.util.List;

public record ResponseUserInfoDto (
        Long id,
        String nickname,
        String avatarInitials,
        int countFollowers,
        int countFollowing,
        Long daysActive,
        List<String> namesTribes
){
}
