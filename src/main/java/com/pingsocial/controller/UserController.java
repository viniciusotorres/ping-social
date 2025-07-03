package com.pingsocial.controller;

import com.pingsocial.api.UserApi;
import com.pingsocial.dto.*;
import com.pingsocial.exception.UserNotFoundException;
import com.pingsocial.models.User;
import com.pingsocial.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador responsável por gerenciar as operações relacionadas aos usuários.
 * Fornece endpoints para criação, autenticação, validação e listagem de usuários.
 *
 */
@RestController
@RequestMapping("/api/users")
public class UserController implements UserApi {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    /**
     * Construtor com injeção de dependências.
     *
     * @param userService Serviço de usuários
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Cria um novo usuário.
     *
     * @param createUserDto Dados do usuário a ser criado
     * @return ResponseEntity contendo os detalhes do usuário criado ou mensagem de erro
     */


    @PostMapping
    public ResponseEntity<ResponseCreateUserDto> createUser(
            @Valid @RequestBody CreateUserDto createUserDto) {
        logger.info("Recebida requisição para criar usuário com email: {}", createUserDto.email());

        try {
            ResponseCreateUserDto createdUser = userService.createUser(createUserDto);
            logger.info("Usuário criado com sucesso: {}", createdUser.email());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao criar usuário: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro ao criar usuário: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Autentica um usuário com base no email e senha fornecidos.
     *
     * @param loginUserDto Dados de login do usuário
     * @param request      Objeto HttpServletRequest para capturar informações da requisição
     * @return ResponseEntity contendo o token JWT ou mensagem de erro
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginUserDto loginUserDto, HttpServletRequest request) {
        logger.info("Recebida requisição de autenticação para o usuário: {}", loginUserDto.email());

        try {
            ResponseEntity<?> response = userService.authenticateUser(loginUserDto, request);
            logger.info("Autenticação processada para o usuário: {}", loginUserDto.email());
            return response;
        } catch (Exception e) {
            logger.error("Erro durante a autenticação do usuário {}: {}", loginUserDto.email(), e.getMessage(), e);
            throw e; // Será tratado pelo GlobalExceptionHandler
        }
    }

    /**
     * Valida o código de ativação de um usuário.
     *
     * @param email Email do usuário
     * @param code  Código de validação enviado por email
     * @return ResponseEntity contendo mensagem de sucesso ou erro
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponseDto> validateUser(@RequestParam String email, @RequestParam String code) {
        logger.info("Recebida requisição para validar código de ativação para o usuário: {}", email);

        try {
            ResponseEntity<String> response = userService.validateUser(email, code);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Código de validação aceito para o usuário: {}", email);
                return ResponseEntity.ok(ApiResponseDto.success(response.getBody()));
            } else {
                logger.warn("Código de validação inválido para o usuário: {}", email);
                return ResponseEntity
                        .status(response.getStatusCode())
                        .body(ApiResponseDto.error(response.getBody()));
            }
        } catch (UserNotFoundException e) {
            logger.error("Usuário não encontrado durante validação: {}", email);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro durante validação do usuário {}: {}", email, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Erro interno do servidor: " + e.getMessage()));
        }
    }

    /**
     * Testa a autenticação de um administrador.
     * Este endpoint requer que o usuário tenha o papel de ADMINISTRATOR.
     *
     * @return ResponseEntity contendo mensagem de sucesso
     */
    @GetMapping("/test/administrator")
    public ResponseEntity<ApiResponseDto> getAuthenticationTest() {
        logger.info("Recebida requisição para testar autenticação de administrador");
        return ResponseEntity.ok(ApiResponseDto.success("Autenticado com sucesso como administrador"));
    }

    /**
     * Testa a autenticação de um cliente.
     * Este endpoint requer que o usuário tenha o papel de CUSTOMER.
     *
     * @return ResponseEntity contendo mensagem de sucesso
     */
    @GetMapping("/test/customer")
    public ResponseEntity<ApiResponseDto> getCustomerAuthenticationTest() {
        logger.info("Recebida requisição para testar autenticação de cliente");
        return ResponseEntity.ok(ApiResponseDto.success("Autenticado com sucesso como cliente"));
    }

    /**
     * Retorna a lista de todos os usuários cadastrados.
     *
     * @return ResponseEntity contendo a lista de usuários
     */
    @GetMapping("/list")
    public ResponseEntity<ListResponseDto<UserDto>> getUsers() {
        logger.info("Recebida requisição para listar todos os usuários");

        try {
            ResponseEntity<List<User>> response = userService.getAllUsers();

            if (response.getBody() != null) {
                List<UserDto> userDtos = response.getBody().stream()
                        .map(UserDto::fromEntity)
                        .collect(Collectors.toList());

                logger.info("Retornando {} usuários", userDtos.size());
                return ResponseEntity.ok(ListResponseDto.success(
                        userDtos,
                        "Usuários obtidos com sucesso"
                ));
            } else {
                logger.warn("Nenhum usuário encontrado");
                return ResponseEntity.ok(ListResponseDto.success(
                        List.of(),
                        "Nenhum usuário encontrado"
                ));
            }
        } catch (Exception e) {
            logger.error("Erro ao obter lista de usuários: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ListResponseDto.error("Erro interno do servidor: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obter sugestões de usuários para o usuário autenticado.
     * Retorna uma lista de sugestões, excluindo o próprio usuário e apenas usuários ativos.
     *
     * @return ResponseEntity contendo um ListResponseDto com as sugestões de usuários ou mensagem de erro.
     */
    @GetMapping("/suggestionsUsers")
    public ResponseEntity<ListResponseDto<ResponseSuggestionUsersDto>> getSuggestionsUsers() {
        logger.info("Recebida requisição para obter sugestões de usuários");

        try {
            List<ResponseSuggestionUsersDto> suggestions = userService.getSuggestionsUsers().getBody();
            if (suggestions == null || suggestions.isEmpty()) {
                logger.warn("Nenhuma sugestão de usuário encontrada");
                return ResponseEntity.ok(ListResponseDto.success(
                        List.of(),
                        "Nenhuma sugestão de usuário encontrada"
                ));
            }
            logger.info("Retornando {} sugestões de usuários", suggestions.size());
            return ResponseEntity.ok(ListResponseDto.success(
                    suggestions,
                    "Sugestões de usuários obtidas com sucesso"
            ));
        } catch (Exception e) {
            logger.error("Erro ao obter sugestões de usuários: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ListResponseDto.error("Erro interno do servidor: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obter informações detalhadas de um usuário pelo seu ID.
     *
     * @param id ID do usuário a ser consultado.
     * @return ResponseEntity contendo ResponseUserInfoDto com os dados do usuário ou status de erro.
     */
    @GetMapping("/myInfo/{id}")
    public ResponseEntity<ResponseUserInfoDto> getMyInfo(
            @PathVariable @Parameter(description = "ID do usuário") Long id) {
        logger.info("Recebida requisição para obter informações do usuário com ID: {}", id);

        try {
            ResponseUserInfoDto userInfo = userService.getMyInfo(id);
            if (userInfo == null) {
                logger.warn("Usuário com ID {} não encontrado", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }
            logger.info("Informações do usuário com ID {} obtidas com sucesso", id);
            return ResponseEntity.ok(userInfo);
        } catch (UserNotFoundException e) {
            logger.error("Usuário não encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        } catch (Exception e) {
            logger.error("Erro ao obter informações do usuário {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Salva a localização do usuário informado.
     *
     * @param userId ID do usuário
     * @param locationDto Dados de localização
     * @return ResponseEntity com mensagem de sucesso ou erro
     */
    @PostMapping("/saveLocation/{userId}")
    public ResponseEntity<ApiResponseDto> saveLocation(
            @PathVariable Long userId,
            @RequestBody LocationDto locationDto) {
        logger.info("Recebida requisição para salvar localização do usuário: {}", userId);

        try {
            userService.saveLocation(userId, locationDto);
            logger.info("Localização salva com sucesso para o usuário: {}", userId);
            return ResponseEntity.ok(ApiResponseDto.success("Localização salva com sucesso"));
        } catch (Exception e) {
            logger.error("Erro ao salvar localização do usuário {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Erro ao salvar localização: " + e.getMessage()));
        }
    }

    /**
     * Obtém a localização (latitude, longitude e distância) do usuário informado.
     *
     * @param userId ID do usuário a ser consultado.
     * @return ResponseEntity contendo LocationDto com os dados de localização ou status de erro.
     */
    @GetMapping("/getLocation/{userId}")
    public ResponseEntity<LocationDto> getLocation(@PathVariable Long userId) {
        logger.info("Recebida requisição para obter localização do usuário: {}", userId);

        try {
            LocationDto location = userService.getLocationById(userId);
            if (location == null) {
                logger.warn("Localização não encontrada para o usuário: {}", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            logger.info("Localização obtida com sucesso para o usuário: {}", userId);
            return ResponseEntity.ok(location);
        } catch (Exception e) {
            logger.error("Erro ao obter localização do usuário {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
