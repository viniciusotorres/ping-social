package com.pingsocial.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para requisição de adição de usuário a uma tribo.
 */
public class TribeJoinRequestDto {

    @NotNull(message = "ID do usuário não pode ser nulo")
    private Long userId;

    @NotNull(message = "ID da tribo não pode ser nulo")
    private Long tribeId;

    public TribeJoinRequestDto() {
    }

    public TribeJoinRequestDto(Long userId, Long tribeId) {
        this.userId = userId;
        this.tribeId = tribeId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTribeId() {
        return tribeId;
    }

    public void setTribeId(Long tribeId) {
        this.tribeId = tribeId;
    }
}