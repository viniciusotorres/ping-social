package com.pingsocial.api;

import com.pingsocial.dto.*;
import com.pingsocial.exception.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(
        name = "Gerenciamento de Usuários",
        description = "Endpoints para operações relacionadas a usuários (CRUD, autenticação, etc.)"
)
public interface UserApi {
    @Operation(
            summary = "Cria um novo usuário",
            description = "Cria um usuário com os dados fornecidos e retorna os detalhes do usuário criado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuário criado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseCreateUserDto.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ResponseCreateUserDto> createUser(
            @Parameter(
                    description = "Dados do usuário a ser criado",
                    required = true,
                    schema = @Schema(implementation = CreateUserDto.class)
            )
            @Valid @RequestBody CreateUserDto createUserDto
    );

    @Operation(
            summary = "Autentica um usuário",
            description = "Autentica um usuário com base no email e senha fornecidos e retorna um token JWT."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário autenticado com sucesso",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<?> authenticateUser(
            @Parameter(
                    description = "Dados de login do usuário",
                    required = true,
                    schema = @Schema(implementation = LoginUserDto.class)
            )
            @Valid @RequestBody LoginUserDto loginUserDto,
            HttpServletRequest request
    );

    @Operation(
            summary = "Valida o código de ativação de um usuário",
            description = "Valida o código de ativação enviado por email para o usuário."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Código validado com sucesso",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "400", description = "Código inválido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ApiResponseDto> validateUser(
            @Parameter(description = "Email do usuário", required = true)
            @RequestParam String email,
            @Parameter(description = "Código de validação enviado por email", required = true)
            @RequestParam String code
    );

    @Operation(
            summary = "Testa autenticação de administrador",
            description = "Endpoint protegido que retorna sucesso se o usuário for ADMINISTRATOR."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticado com sucesso como administrador",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
            ),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    ResponseEntity<ApiResponseDto> getAuthenticationTest();

    @Operation(
            summary = "Testa autenticação de cliente",
            description = "Endpoint protegido que retorna sucesso se o usuário for CUSTOMER."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticado com sucesso como cliente",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
            ),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    ResponseEntity<ApiResponseDto> getCustomerAuthenticationTest();

    @Operation(
            summary = "Lista todos os usuários",
            description = "Retorna a lista de todos os usuários cadastrados."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuários obtida com sucesso",
                    content = @Content(schema = @Schema(implementation = ListResponseDto.class))
            ),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ListResponseDto<UserDto>> getUsers();


    @Operation(
            summary = "Sugestões de usuários",
            description = "Retorna sugestões de usuários para o usuário autenticado, excluindo ele mesmo e apenas usuários ativos."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sugestões de usuários obtidas com sucesso",
                    content = @Content(schema = @Schema(implementation = ListResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor"
            )
    })
    @GetMapping("/suggestionsUsers")
    ResponseEntity<ListResponseDto<ResponseSuggestionUsersDto>> getSuggestionsUsers();

    @Operation(
            summary = "Obtém informações do usuário",
            description = "Retorna informações detalhadas do usuário pelo ID informado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Informações do usuário obtidas com sucesso",
                    content = @Content(schema = @Schema(implementation = ResponseUserInfoDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor"
            )
    })
    @GetMapping("/myInfo/{id}")
    ResponseEntity<ResponseUserInfoDto> getMyInfo(
            @PathVariable
            @Parameter(description = "ID do usuário", required = true)
            Long id
    );

    /**
     * Salva a localização (latitude e longitude) do usuário informado.
     *
     * @param userId      ID do usuário a ter a localização atualizada.
     * @param locationDto Objeto contendo latitude e longitude.
     * @return ResponseEntity com mensagem de sucesso ou erro.
     */
    @Operation(
            summary = "Salva a localização do usuário",
            description = "Salva a latitude e longitude do usuário informado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Localização salva com sucesso",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro ao salvar localização"
            )
    })
    ResponseEntity<ApiResponseDto> saveLocation(
            @PathVariable
            @Parameter(description = "ID do usuário", required = true)
            Long userId,
            @RequestBody
            @Parameter(description = "Dados de localização (latitude e longitude)", required = true)
            LocationDto locationDto
    );

    /**
     * Obtém a localização (latitude, longitude e distância) do usuário informado.
     *
     * @param userId ID do usuário a ser consultado.
     * @return ResponseEntity contendo LocationDto com os dados de localização ou status de erro.
     */
    @Operation(
            summary = "Obtém a localização do usuário",
            description = "Retorna a latitude, longitude e distância (em km) do usuário informado em relação ao usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Localização obtida com sucesso",
                    content = @Content(schema = @Schema(implementation = LocationDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Localização não encontrada"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro ao obter localização"
            )
    })
    ResponseEntity<LocationDto> getLocation(
            @PathVariable
            @Parameter(description = "ID do usuário", required = true)
            Long userId
    );

    /**
     * Inicia o processo de recuperação de senha enviando um email com código de validação.
     *
     * @param forgotPasswordDto DTO contendo o email do usuário
     * @return ResponseEntity com mensagem de sucesso ou erro
     */
    @Operation(
            summary = "Solicita recuperação de senha",
            description = "Envia um email com código de validação para recuperação de senha do usuário."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Email de recuperação enviado com sucesso",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor"
            )
    })
    ResponseEntity<ApiResponseDto> forgotPassword(
            @Valid @RequestBody ForgotPasswordDto forgotPasswordDto
    );

    /**
     * Redefine a senha do usuário após validação do código enviado por email.
     *
     * @param resetPasswordDto DTO contendo email, código de validação e nova senha
     * @return ResponseEntity com mensagem de sucesso ou erro
     */
    @Operation(
            summary = "Redefine a senha do usuário",
            description = "Redefine a senha do usuário após validação do código enviado por email."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Senha redefinida com sucesso",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Código de validação inválido"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor"
            )
    })
    ResponseEntity<ApiResponseDto> resetPassword(
            @Valid @RequestBody ResetPasswordDto resetPasswordDto
    );

}