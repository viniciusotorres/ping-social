package com.pingsocial.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para requisição de adição de usuário a uma tribo.
 */
public class TribeJoinRequestDto {

    @NotNull(message = "ID da tribo não pode ser nulo")
    private Long tribeId;

    public TribeJoinRequestDto() {
    }

    public TribeJoinRequestDto(Long tribeId) {
        this.tribeId = tribeId;
    }

    public Long getTribeId() {
        return tribeId;
    }

    public void setTribeId(Long tribeId) {
        this.tribeId = tribeId;
    }
}