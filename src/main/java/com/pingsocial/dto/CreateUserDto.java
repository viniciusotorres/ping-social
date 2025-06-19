package com.pingsocial.dto;

import com.pingsocial.models.RoleName;

public record CreateUserDto(

        String email,
        String password,
        RoleName role,
        Double latitude,
        Double longitude

) {
}