package com.pingsocial.controller;

import com.pingsocial.api.PostApi;
import com.pingsocial.dto.CreatePostDto;
import com.pingsocial.dto.ListResponseDto;
import com.pingsocial.dto.ResponsePost;
import com.pingsocial.models.PostFilterType;
import com.pingsocial.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController implements PostApi {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Override
    public ResponseEntity<ListResponseDto<ResponsePost>> getPosts(
            Long userId,
            PostFilterType filterType,
            Pageable pageable) {
        Page<ResponsePost> postPage = postService.getPosts(userId, filterType, pageable);
        ListResponseDto<ResponsePost> response = ListResponseDto.success(
                postPage.getContent(),
                "Posts obtidos com sucesso"
        );
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ResponsePost> createPost(@RequestBody CreatePostDto request) {
        ResponsePost createdPost = postService.createPost(request);
        return ResponseEntity.ok(createdPost);
    }
}