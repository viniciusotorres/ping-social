package com.pingsocial.api;

import com.pingsocial.dto.ApiResponseDto;
import com.pingsocial.dto.FollowUserRequestDto;
import com.pingsocial.dto.ListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(
        name = "Gerenciamento de Seguidores",
        description = "Endpoints para operações de seguir, deixar de seguir e consultar seguidores/seguidos."
)
public interface FollowApi {

    @Operation(
            summary = "Segue um usuário",
            description = "Permite que o usuário autenticado siga outro usuário."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário seguido com sucesso", content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ApiResponseDto> followUser(
            @Parameter(description = "DTO com o ID do usuário a ser seguido", required = true)
            @Valid @RequestBody FollowUserRequestDto request
    );

    @Operation(
            summary = "Deixa de seguir um usuário",
            description = "Permite que o usuário autenticado deixe de seguir outro usuário."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário deixado de ser seguido com sucesso", content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ApiResponseDto> unfollowUser(
            @Parameter(description = "DTO com o ID do usuário a deixar de seguir", required = true)
            @Valid @RequestBody FollowUserRequestDto request
    );

    @Operation(
            summary = "Obtém a lista de seguidores de um usuário",
            description = "Retorna a lista de seguidores do usuário informado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seguidores obtidos com sucesso", content = @Content(schema = @Schema(implementation = ListResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ListResponseDto> getFollowers(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long userId
    );

    @Operation(
            summary = "Obtém a lista de usuários que um usuário está seguindo",
            description = "Retorna a lista de usuários que o usuário informado está seguindo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seguidos obtidos com sucesso", content = @Content(schema = @Schema(implementation = ListResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ListResponseDto> getFollowing(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long userId
    );

    @Operation(
            summary = "Verifica se o usuário autenticado está seguindo outro usuário",
            description = "Retorna se o usuário autenticado está seguindo o usuário informado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso", content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ApiResponseDto> isFollowing(
            @Parameter(description = "ID do usuário a ser verificado", required = true)
            @PathVariable Long followUserId
    );
}