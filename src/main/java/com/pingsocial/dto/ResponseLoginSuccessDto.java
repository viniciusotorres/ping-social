package com.pingsocial.dto;

public record ResponseLoginSuccessDto(
        String token,
        Long id,
        String email,
        Double latitude,
        Double longitude,
        boolean ativo,
        String nickname
) {
}
