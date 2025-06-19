package com.pingsocial.dto;

import com.pingsocial.models.Role;

import java.util.List;

public record RecoveryUserDto(

        Long id,
        String email,
        List<Role> roles

) {
}