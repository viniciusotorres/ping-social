package com.pingsocial.controller;

import com.pingsocial.api.TribeApi;
import com.pingsocial.dto.*;
import com.pingsocial.exception.TribeNotFoundException;
import com.pingsocial.exception.UserAlreadyInTribeException;
import com.pingsocial.exception.UserNotFoundException;
import com.pingsocial.models.Tribe;
import com.pingsocial.models.User;
import com.pingsocial.service.TribeService;
import com.pingsocial.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller para operações relacionadas a tribos.
 */
@RestController
@RequestMapping("/api/tribes")
public class TribeController implements TribeApi {

    private static final Logger logger = LoggerFactory.getLogger(TribeController.class);
    private final TribeService tribeService;
    private final UserService userService;

    public TribeController(TribeService tribeService, UserService userService) {
        this.tribeService = tribeService;
        this.userService = userService;
    }

    /**
     * Adiciona um usuário a uma tribo.
     *
     * @param request DTO contendo o ID do usuário e o ID da tribo
     * @return ResponseEntity com mensagem de sucesso ou erro
     */
    @PostMapping("/join")
    public ResponseEntity<ApiResponseDto> joinTribe(@Valid @RequestBody TribeJoinRequestDto request) {
        logger.info("Recebida requisição para adicionar usuário à tribo {}", request.getTribeId());

        try {

            String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            Long userId = userService.findIdByEmail(email);

            tribeService.joinTribe(userId, request.getTribeId());
            logger.info("Usuário {} adicionado com sucesso à tribo {}", userId, request.getTribeId());

            return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("Usuário adicionado à tribo com sucesso!"));
        } catch (UserNotFoundException e) {
            logger.error("Erro ao adicionar usuário à tribo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDto.error(e.getMessage()));
        } catch (TribeNotFoundException e) {
            logger.error("Erro ao adicionar usuário à tribo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDto.error(e.getMessage()));
        } catch (UserAlreadyInTribeException e) {
            logger.warn("Usuário já é membro da tribo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponseDto.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao adicionar usuário à tribo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponseDto.error("Erro interno do servidor: " + e.getMessage()));
        }
    }

    /**
     * Obtém todas as tribos das quais um usuário é membro.
     *
     * @param userId ID do usuário
     * @return ResponseEntity com a lista de tribos
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<ListResponseDto<TribeDto>> getUserTribes(@PathVariable Long userId) {
        logger.info("Recebida requisição para obter tribos do usuário {}", userId);

        try {
            Set<Tribe> tribes = tribeService.getUserTribes(userId);
            List<TribeDto> tribeDtos = tribes.stream().map(TribeDto::fromEntity).collect(Collectors.toList());

            logger.info("Retornando {} tribos para o usuário {}", tribeDtos.size(), userId);
            return ResponseEntity.ok(ListResponseDto.success(tribeDtos, "Tribos do usuário obtidas com sucesso"));
        } catch (UserNotFoundException e) {
            logger.error("Erro ao obter tribos do usuário: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ListResponseDto.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ListResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao obter tribos do usuário: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ListResponseDto.error("Erro interno do servidor: " + e.getMessage()));
        }
    }

    /**
     * Obtém todos os membros de uma tribo.
     *
     * @param tribeId ID da tribo
     * @return ResponseEntity com a lista de membros
     */
    @GetMapping("/{tribeId}/members")
    public ResponseEntity<ListResponseDto<UserDto>> getTribeMembers(@PathVariable Long tribeId) {
        logger.info("Recebida requisição para obter membros da tribo {}", tribeId);

        try {
            Set<User> members = tribeService.getTribeMembers(tribeId);
            List<UserDto> userDtos = members.stream().map(UserDto::fromEntity).collect(Collectors.toList());

            logger.info("Retornando {} membros para a tribo {}", userDtos.size(), tribeId);
            return ResponseEntity.ok(ListResponseDto.success(userDtos, "Membros da tribo obtidos com sucesso"));
        } catch (TribeNotFoundException e) {
            logger.error("Erro ao obter membros da tribo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ListResponseDto.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ListResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao obter membros da tribo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ListResponseDto.error("Erro interno do servidor: " + e.getMessage()));
        }
    }

    /**
     * Remove um usuário de uma tribo.
     *
     * @param request DTO contendo o ID do usuário e o ID da tribo
     * @return ResponseEntity com mensagem de sucesso ou erro
     */
    @PostMapping("/leave")
    public ResponseEntity<ApiResponseDto> leaveTribe(@Valid @RequestBody TribeJoinRequestDto request) {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Recebida requisição para remover usuário autenticado da tribo {}", request.getTribeId());

        try {
            Long userId = userService.findIdByEmail(email);

            tribeService.leaveTribe(userId, request.getTribeId());
            logger.info("Usuário {} removido com sucesso da tribo {}", userId, request.getTribeId());

            return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("Usuário removido da tribo com sucesso!"));
        } catch (UserNotFoundException e) {
            logger.error("Erro ao remover usuário da tribo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDto.error(e.getMessage()));
        } catch (TribeNotFoundException e) {
            logger.error("Erro ao remover usuário da tribo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDto.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao remover usuário da tribo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponseDto.error("Erro interno do servidor: " + e.getMessage()));
        }
    }


    /**
     * Obtém todas as tribos disponíveis.
     *
     * @return ResponseEntity com a lista de tribos
     */
    @GetMapping("/all")
    public ResponseEntity<ListResponseDto<TribeDto>> getAllTribes() {
        logger.info("Recebida requisição para obter todas as tribos");

        try {
            List<Tribe> tribes = tribeService.listAllTribes();
            List<TribeDto> tribeDtos = tribes.stream().map(TribeDto::fromEntity).collect(Collectors.toList());

            logger.info("Retornando {} tribos disponíveis", tribeDtos.size());
            return ResponseEntity.ok(ListResponseDto.success(tribeDtos, "Todas as tribos obtidas com sucesso"));
        } catch (Exception e) {
            logger.error("Erro inesperado ao obter tribos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ListResponseDto.error("Erro interno do servidor: " + e.getMessage()));
        }
    }

    @GetMapping("/has-tribe")
    public ResponseEntity<ApiResponseDto> userHasAnyTribe() {
        logger.info("Recebida requisição para verificar se o usuário autenticado já possui tribo");
        try {
            boolean hasTribe = tribeService.userHasAnyTribe();
            return ResponseEntity.ok(ApiResponseDto.success(hasTribe, hasTribe ? "Usuário já possui uma tribo" : "Usuário não possui tribo"));
        } catch (Exception e) {
            logger.error("Erro ao verificar se usuário possui tribo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponseDto.error("Erro interno do servidor: " + e.getMessage()));
        }
    }

}
