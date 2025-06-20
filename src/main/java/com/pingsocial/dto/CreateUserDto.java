package com.pingsocial.dto;

import com.pingsocial.models.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserDto(
        @Email(message = "O email deve ser válido.")
        @NotBlank(message = "O email não pode estar vazio.")
        String email,

        @NotBlank(message = "A senha não pode estar vazia.")
        String password,

        @NotNull(message = "O papel do usuário não pode ser nulo.")
        RoleName role

) {
}