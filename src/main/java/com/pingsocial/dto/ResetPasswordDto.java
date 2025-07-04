package com.pingsocial.dto;

public record ResetPasswordDto(
        String email,
        String code,
        String newPassword
) {
}
