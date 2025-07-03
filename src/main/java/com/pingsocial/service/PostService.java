package com.pingsocial.service;

import com.pingsocial.dto.CreatePostDto;
import com.pingsocial.dto.FollowListDto;
import com.pingsocial.dto.ResponsePost;
import com.pingsocial.exception.UserNotFoundException;
import com.pingsocial.models.Post;
import com.pingsocial.models.PostFilterType;
import com.pingsocial.models.Tribe;
import com.pingsocial.models.User;
import com.pingsocial.repository.PostRepository;
import com.pingsocial.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {
    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TribeService tribeService;
    private final FollowService followService;

    public PostService(PostRepository postRepository, UserRepository userRepository,
                       TribeService tribeService, FollowService followService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.tribeService = tribeService;
        this.followService = followService;
    }

    public Page<ResponsePost> getPosts(Long userId, PostFilterType filterType, Pageable pageable) {
        logger.info("Obtendo posts para o usuário {} com filtro {}", userId, filterType);

        if (userId == null || filterType == null) {
            logger.error("IDs de usuário ou tipo de filtro não podem ser nulos. userId={}, filterType={}", userId, filterType);
            throw new IllegalArgumentException("IDs de usuário e tipo de filtro não podem ser nulos");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Usuário não encontrado com ID: {}", userId);
                    return new UserNotFoundException(userId);
                });

        logger.info("Usuário encontrado: {}", user.getNickname());

        Page<Post> postPage;

        switch (filterType) {
            case ALL:
                postPage = postRepository.findAll(pageable);
                break;
            case MY_POSTS:
                postPage = postRepository.findByAuthor_Id(userId, pageable);
                break;
            case TRIBE_POSTS:
                Set<Long> tribeIds = tribeService.getTribeIdsByUserId(userId);
                if (tribeIds.isEmpty()) {
                    logger.warn("Usuário {} não pertence a nenhuma tribo", userId);
                    return Page.empty(pageable);
                }
                postPage = postRepository.findByTribes_IdIn(tribeIds, pageable);
                break;
            case FRIENDS_POSTS:
                List<FollowListDto> friends = followService.getFollowing(userId);
                List<Long> friendUserIds = friends.stream()
                        .map(FollowListDto::followedUserId)
                        .collect(Collectors.toList());
                if (friendUserIds.isEmpty()) {
                    logger.warn("Usuário {} não segue ninguém", userId);
                    return Page.empty(pageable);
                }
                postPage = postRepository.findByAuthor_IdIn(friendUserIds, pageable);
                break;
            default:
                throw new IllegalArgumentException("Tipo de filtro de post desconhecido: " + filterType);
        }


        logger.info("Posts obtidos com sucesso: {} posts encontrados", postPage.getTotalElements());
        return postPage.map(post -> new ResponsePost(
                post.getId(),
                post.getAuthor().getNickname(),
                post.getTribes().stream().map(Tribe::getId).collect(Collectors.toSet()),
                post.getContent(),
                post.getCreatedAt()
        ));
    }

    public ResponsePost createPost(CreatePostDto createPostDto) {
        logger.info("Criando post para o usuário {}", createPostDto.getUserId());

        User user = userRepository.findById(createPostDto.getUserId())
                .orElseThrow(() -> {
                    logger.error("Usuário não encontrado com ID: {}", createPostDto.getUserId());
                    return new UserNotFoundException(createPostDto.getUserId());
                });

        Post post = new Post();
        post.setAuthor(user);
        post.setContent(createPostDto.getContent());
        post.setCreatedAt(createPostDto.setCreatedAt());

        Set<Tribe> tribes = new HashSet<>();
        if (createPostDto.getTribeIds() != null) {
            for (Long tribeId : createPostDto.getTribeIds()) {
                Tribe tribe = tribeService.getTribeById(tribeId);
                if (tribe == null) {
                    logger.warn("Tribo com ID {} não encontrada, ignorando na criação do post", tribeId);
                    continue;
                }
                if (!tribe.getMembers().contains(user)) {
                    logger.warn("Usuário {} não é membro da tribo {}", user.getId(), tribeId);
                    continue;
                }
                tribes.add(tribe);
            }
        }
        post.setTribes(tribes);

        Post savedPost = postRepository.save(post);
        logger.info("Post criado com sucesso: {}", savedPost.getId());

        return new ResponsePost(
                savedPost.getId(),
                savedPost.getAuthor().getNickname(),
                savedPost.getTribes().stream().map(Tribe::getId).collect(Collectors.toSet()),
                savedPost.getContent(),
                savedPost.getCreatedAt()
        );
    }
}