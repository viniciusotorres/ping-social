package com.pingsocial.api;

import com.pingsocial.dto.CreatePostDto;
import com.pingsocial.dto.ListResponseDto;
import com.pingsocial.dto.ResponsePost;
import com.pingsocial.models.PostFilterType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Posts",
        description = "Endpoints para operações de listagem de posts com filtros."
)
public interface PostApi {

    @Operation(
            summary = "Obtém uma lista de posts filtrados por usuário",
            description = "Retorna posts conforme o filtro selecionado e o usuário informado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Posts obtidos com sucesso", content = @Content(schema = @Schema(implementation = ListResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/{userId}")
    ResponseEntity<ListResponseDto<ResponsePost>> getPosts(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Tipo de filtro de post", required = true)
            @RequestParam PostFilterType filterType,
            @Parameter(hidden = true) Pageable pageable
    );

    @Operation(
            summary = "Cria um novo post",
            description = "Cria um post para o usuário informado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Post criado com sucesso", content = @Content(schema = @Schema(implementation = ResponsePost.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping
    ResponseEntity<ResponsePost> createPost(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para criação do post",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreatePostDto.class))
            )
            @RequestBody CreatePostDto request
    );
}