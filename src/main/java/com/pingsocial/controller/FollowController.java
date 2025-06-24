package com.pingsocial.controller;

import com.pingsocial.dto.ApiResponseDto;
import com.pingsocial.dto.FollowUserRequestDto;
import com.pingsocial.dto.ListResponseDto;
import com.pingsocial.service.FollowService;
import com.pingsocial.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para operações relacionadas a seguidores.
 */
@RestController
@RequestMapping("/api/follow")
public class FollowController {

    private static final Logger logger = LoggerFactory.getLogger(FollowController.class);
    private final FollowService followService;
    private final UserService userService;

    public FollowController(FollowService followService, UserService userService) {
        this.followService = followService;
        this.userService = userService;
    }

    /**
     * Segue um usuário.
     *
     * @param request DTO contendo o ID do usuário que está seguindo e o ID do usuário a ser seguido
     * @return ResponseEntity com mensagem de sucesso ou erro
     */
    @PostMapping("/follow")
    public ResponseEntity<ApiResponseDto> followUser(@Valid @RequestBody FollowUserRequestDto request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userService.findIdByEmail(email);
        Long followUserId = request.followUserId();
        logger.info("Recebida requisição para seguir usuário {} por usuário {}", followUserId, userId);

        try {
            followService.followUser(userId, followUserId);
            logger.info("Usuário {} seguido com sucesso por usuário {}", followUserId, userId);
            return ResponseEntity.ok(ApiResponseDto.success("Usuário seguido com sucesso!"));
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao seguir usuário: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao seguir usuário", e);
            return ResponseEntity.status(500).body(ApiResponseDto.error("Erro interno do servidor"));
        }
    }

    /**
     * Deixa de seguir um usuário.
     *
     * @param request DTO contendo o ID do usuário que está seguindo e o ID do usuário a deixar de seguido
     * @return ResponseEntity com mensagem de sucesso ou erro
     */
    @PostMapping("/unfollow")
    public ResponseEntity<ApiResponseDto> unfollowUser(@Valid @RequestBody FollowUserRequestDto request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userService.findIdByEmail(email);
        Long followUserId = request.followUserId();
        logger.info("Recebida requisição para deixar de seguir  usuário {} por usuário {}", followUserId, userId);

        try {
            followService.unfollowUser(userId, followUserId);
            logger.info("Usuário {} deixado de ser seguido com sucesso por usuário {}", followUserId, userId);
            return ResponseEntity.ok(ApiResponseDto.success("Usuário deixado de ser seguido com sucesso!"));
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao deixar de esguir usuário: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao deixar de esguir usuário", e);
            return ResponseEntity.status(500).body(ApiResponseDto.error("Erro interno do servidor"));
        }
    }

    /**
     * Obtém a lista de seguidores de um usuário.
     *
     * @param userId ID do usuário cujos seguidores serão obtidos
     * @return ResponseEntity com a lista de seguidores ou erro
     */
    @GetMapping("/followers/{userId}")
    public ResponseEntity<ListResponseDto> getFollowers(@PathVariable Long userId) {
        logger.info("Recebida requisição para obter seguidores do usuário {}", userId);

        try {
            var followers = followService.getFollowers(userId);
            logger.info("Lista de seguidores obtida com sucesso para o usuário {}", userId);
            return ResponseEntity.ok(ListResponseDto.success(followers, "Seguidores obtidos com sucesso"));
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao obter seguidores: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ListResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao obter seguidores", e);
            return ResponseEntity.status(500).body(ListResponseDto.error("Erro interno do servidor"));
        }
    }

    /**
     * Obtém a lista de usuários que um usuário está seguindo.
     *
     * @param userId ID do usuário cujos seguidos serão obtidos
     * @return ResponseEntity com a lista de seguidos ou erro
     */
    @GetMapping("/following/{userId}")
    public ResponseEntity<ListResponseDto> getFollowing(@PathVariable Long userId) {
        logger.info("Recebida requisição para obter usuários que o usuário {} está seguindo", userId);

        try {
            var following = followService.getFollowing(userId);
            logger.info("Lista de usuários seguidos obtida com sucesso para o usuário {}", userId);
            return ResponseEntity.ok(ListResponseDto.success(following, "Usuários seguidos obtidos com sucesso"));
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao obter seguidos: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ListResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao obter seguidos", e);
            return ResponseEntity.status(500).body(ListResponseDto.error("Erro interno do servidor"));
        }
    }

    /**
     * Verifica se um usuário está seguindo outro usuário.
     *
     * @param userId       ID do usuário que está verificando
     * @param followUserId ID do usuário a ser verificado
     * @return ResponseEntity com o resultado da verificação
     */
    @GetMapping("/is-following/{followUserId}")
    public ResponseEntity<ApiResponseDto> isFollowing(@PathVariable Long followUserId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userService.findIdByEmail(email);
        logger.info("Recebida requisição para verificar se usuário {} está seguindo o usuário {}", userId, followUserId);

        try {
            boolean isFollowing = followService.isFollowing(userId, followUserId);
            logger.info("Usuário {} está seguindo o usuário {}: {}", userId, followUserId, isFollowing);
            return ResponseEntity.ok(ApiResponseDto.success(isFollowing ? "Usuário está seguindo" : "Usuário não está seguindo"));
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao verificar seguimento: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao verificar seguimento", e);
            return ResponseEntity.status(500).body(ApiResponseDto.error("Erro interno do servidor"));
        }
    }
}
