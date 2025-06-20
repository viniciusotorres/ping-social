package com.pingsocial.dto;

public record ResponseIsNotActive(
    String message,
    String email
) {
}
