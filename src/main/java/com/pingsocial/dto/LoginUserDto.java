package com.pingsocial.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginUserDto(
        @Email(message = "O email deve ser válido")
        @NotBlank(message = "O email não pode estar vazio")
        String email,

        @NotBlank(message = "A senha não pode estar vazia")
        String password
) {
}
