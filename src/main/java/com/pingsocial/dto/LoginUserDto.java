package com.pingsocial.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginUserDto(
        @Email(message = "O email deve ser válido")
        @NotBlank(message = "O email não pode estar vazio")
        @Size(max = 100, message = "O email não pode ter mais de 100 caracteres")
        String email,

        @NotBlank(message = "A senha não pode estar vazia")
        @Size(max = 50, message = "A senha não pode ter mais de 50 caracteres")
        String password
) {
}
