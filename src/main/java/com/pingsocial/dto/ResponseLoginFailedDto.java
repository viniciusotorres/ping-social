package com.pingsocial.dto;

public record ResponseLoginFailedDto(
    String message,
    String email
) {
}
