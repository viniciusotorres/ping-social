package com.pingsocial.api;

import com.pingsocial.dto.*;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(
        name = "Gerenciamento de Tribos",
        description = "Endpoints para operações relacionadas a tribos (criação, associação, listagem, etc.)"
)
public interface TribeApi {

    @Operation(
            summary = "Adiciona um usuário a uma tribo",
            description = "Adiciona o usuário autenticado à tribo informada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário adicionado à tribo com sucesso", content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuário ou tribo não encontrado"),
            @ApiResponse(responseCode = "409", description = "Usuário já é membro da tribo"),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ApiResponseDto> joinTribe(
            @Parameter(description = "DTO contendo o ID da tribo", required = true)
            @Valid @RequestBody TribeJoinRequestDto request
    );

    @Operation(
            summary = "Obtém todas as tribos de um usuário",
            description = "Retorna a lista de tribos das quais o usuário informado é membro."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tribos do usuário obtidas com sucesso", content = @Content(schema = @Schema(implementation = ListResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ListResponseDto<TribeDto>> getUserTribes(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long userId
    );

    @Operation(
            summary = "Obtém todos os membros de uma tribo",
            description = "Retorna a lista de membros da tribo informada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membros da tribo obtidos com sucesso", content = @Content(schema = @Schema(implementation = ListResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Tribo não encontrada"),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ListResponseDto<UserDto>> getTribeMembers(
            @Parameter(description = "ID da tribo", required = true)
            @PathVariable Long tribeId
    );

    @Operation(
            summary = "Remove um usuário de uma tribo",
            description = "Remove o usuário autenticado da tribo informada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário removido da tribo com sucesso", content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuário ou tribo não encontrado"),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ApiResponseDto> leaveTribe(
            @Parameter(description = "DTO contendo o ID da tribo", required = true)
            @Valid @RequestBody TribeJoinRequestDto request
    );

    @Operation(
            summary = "Obtém todas as tribos disponíveis",
            description = "Retorna a lista de todas as tribos cadastradas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Todas as tribos obtidas com sucesso", content = @Content(schema = @Schema(implementation = ListResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ListResponseDto<TribeDto>> getAllTribes();

    @Operation(
            summary = "Verifica se o usuário autenticado já possui tribo",
            description = "Retorna true se o usuário autenticado já faz parte de alguma tribo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso", content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<ApiResponseDto> userHasAnyTribe();
}