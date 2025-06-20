package com.pingsocial.dto;

public record ResponseCreateUserDto(
    String message,
    Long id,
    String email,
    String CreatedAt
) {
}
