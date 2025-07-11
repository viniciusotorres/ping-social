package com.pingsocial.dto;

import com.pingsocial.models.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserDto(
        @Email(message = "O email deve ser válido.")
        @NotBlank(message = "O email não pode estar vazio.")
        @Size(max = 100, message = "O email não pode ter mais de 100 caracteres.")
        String email,

        @NotBlank(message = "A senha não pode estar vazia.")
        @Size(max = 50, message = "A senha não pode ter mais de 50 caracteres.")
        String password,

        @NotBlank(message = "O apelido não pode estar vazio.")
        @Size(min = 3, max = 30, message = "O apelido deve ter entre 3 e 30 caracteres.")
        String nickname,

        @NotNull(message = "O papel do usuário não pode ser nulo.")
        RoleName role
) {
}
