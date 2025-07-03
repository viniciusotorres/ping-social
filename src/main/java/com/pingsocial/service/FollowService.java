package com.pingsocial.service;

import com.pingsocial.dto.FollowListDto;
import com.pingsocial.models.Follow;
import com.pingsocial.models.User;
import com.pingsocial.repository.FollowRepository;
import com.pingsocial.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FollowService {

    private static final Logger logger = LoggerFactory.getLogger(FollowService.class);

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public FollowService(UserRepository userRepository, FollowRepository followRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

    /**
     * Segue um usuário.
     *
     * @param userId       ID do usuário que está seguindo
     * @param followUserId ID do usuário a ser seguido
     * @throws IllegalArgumentException se userId ou followUserId forem nulos
     */
    public void followUser(Long userId, Long followUserId) {
        // Validação de entrada
        if (userId == null || followUserId == null) {
            logger.error("Tentativa de seguir usuário com ID nulo. userId={}, followUserId={}", userId, followUserId);
            throw new IllegalArgumentException("IDs de usuário e usuário a ser seguido não podem ser nulos");
        }

        logger.info("Iniciando processo de seguir usuário {} por usuário {}", followUserId, userId);


        // Verifica se o usuário que está seguindo existe
        var followerOpt = userRepository.findById(userId);
        if (followerOpt.isEmpty()) {
            logger.error("Usuário não encontrado com ID: {}", userId);
            throw new IllegalArgumentException("Usuário não encontrado");
        }

        // Verifica se o usuário a ser seguido existe
        var followedOpt = userRepository.findById(followUserId);
        if (followedOpt.isEmpty()) {
            logger.error("Usuário a ser seguido não encontrado com ID: {}", followUserId);
            throw new IllegalArgumentException("Usuário a ser seguido não encontrado");
        }

        // Verifica se o usuário já está seguindo o outro usuário
        if (followRepository.existsByFollowerIdAndFollowedId(userId, followUserId)) {
            logger.warn("Usuário {} já segue o usuário {}", userId, followUserId);
            throw new IllegalArgumentException("Usuário já está seguindo este usuário");
        }

        // Verifica se o usuário está tentando seguir a si mesmo
        if (userId.equals(followUserId)) {
            logger.warn("Usuário {} tentou seguir a si mesmo", userId);
            throw new IllegalArgumentException("Você não pode seguir a si mesmo.");
        }


        // Cria a relação de seguimento
        Follow follow = new Follow();
        follow.setFollower(followerOpt.get());
        follow.setFollowed(followedOpt.get());
        follow.setCreatedAt(java.time.LocalDateTime.now());
        followRepository.save(follow);

        logger.info("Usuário {} agora segue o usuário {}", userId, followUserId);
    }

    /**
     * Desfaz o seguimento de um usuário.
     *
     * @param userId       ID do usuário que está desfazendo o seguimento
     * @param followUserId ID do usuário a ser deixado de seguir
     * @throws IllegalArgumentException se userId ou followUserId forem nulos
     */
    @Transactional
    public void unfollowUser(Long userId, Long followUserId) {
        // Validação de entrada
        if (userId == null || followUserId == null) {
            logger.error("Tentativa  para deixar de seguir com ID nulo. userId={}, followUserId={}", userId, followUserId);
            throw new IllegalArgumentException("IDs de usuário e usuário a ser deixado de seguir não podem ser nulos");
        }

        logger.info("Iniciando processo para deixar de seguir usuário {} por usuário {}", followUserId, userId);

        // Verifica se o usuário que está desfazendo o seguimento existe
        var followerOpt = userRepository.findById(userId);
        if (followerOpt.isEmpty()) {
            logger.error("Usuário não encontrado com ID: {}", userId);
            throw new IllegalArgumentException("Usuário não encontrado");
        }

        // Verifica se o usuário a ser deixado de seguir existe
        var followedOpt = userRepository.findById(followUserId);
        if (followedOpt.isEmpty()) {
            logger.error("Usuário a ser deixado de seguir não encontrado com ID: {}", followUserId);
            throw new IllegalArgumentException("Usuário a ser deixado de seguir não encontrado");
        }

        // Verifica se o usuário realmente segue o outro usuário
        if (!followRepository.existsByFollowerIdAndFollowedId(userId, followUserId)) {
            logger.warn("Usuário {} não segue o usuário {}", userId, followUserId);
            throw new IllegalArgumentException("Usuário não está seguindo este usuário");
        }

        // Remove a relação de seguimento
        followRepository.deleteByFollowerIdAndFollowedId(userId, followUserId);

        logger.info("Usuário {} deixou de seguir o usuário {}", userId, followUserId);
    }

    /**
     * Verifica se um usuário está seguindo outro usuário.
     *
     * @param userId       ID do usuário que está verificando
     * @param followUserId ID do usuário a ser verificado
     * @return true se o usuário está seguindo, false caso contrário
     * @throws IllegalArgumentException se userId ou followUserId forem nulos ou se o usuário tentar verificar a si mesmo
     */
    public boolean isFollowing(Long userId, Long followUserId) {
        if (userId == null || followUserId == null) {
            logger.error("Tentativa de verificar seguimento com ID nulo. userId={}, followUserId={}", userId, followUserId);
            throw new IllegalArgumentException("IDs de usuário e usuário a ser verificado não podem ser nulos");
        }

        if (userId.equals(followUserId)) {
            logger.warn("Usuário {} tentou verificar se segue a si mesmo", userId);
            throw new IllegalArgumentException("Não é possível verificar se está seguindo a si mesmo.");
        }

        return followRepository.existsByFollowerIdAndFollowedId(userId, followUserId);
    }


    /**
     * Obtém a lista de seguidores de um usuário.
     *
     * @param userId ID do usuário cujos seguidores serão obtidos
     * @return Lista de seguidores do usuário
     * @throws IllegalArgumentException se userId for nulo
     */
    public List<FollowListDto> getFollowers(Long userId) {
        if (userId == null) {
            logger.error("Tentativa de obter seguidores com ID nulo. userId={}", userId);
            throw new IllegalArgumentException("ID de usuário não pode ser nulo");
        }

        logger.info("Obtendo seguidores para o usuário com ID {}", userId);
        List<Follow> follows = followRepository.findByFollowedId(userId);
        if (follows.isEmpty()) {
            logger.info("Usuário com ID {} não tem seguidores", userId);
            return List.of();
        }

        List<FollowListDto> followers = follows.stream()
                .map(follow -> new FollowListDto(follow.getFollower().getId(), follow.getFollower().getEmail(), follow.getFollower().getNickname()))
                .toList();

        logger.info("Usuário com ID {} tem {} seguidores", userId, followers.size());
        return followers;
    }

    /**
     * Obtém a lista de usuários que um usuário está seguindo.
     *
     * @param userId ID do usuário cujos seguidos serão obtidos
     * @return Lista de usuários que o usuário está seguindo
     * @throws IllegalArgumentException se userId for nulo
     */
    public List<FollowListDto> getFollowing(Long userId) {
        if (userId == null) {
            logger.error("Tentativa de obter seguidos com ID nulo. userId={}", userId);
            throw new IllegalArgumentException("ID de usuário não pode ser nulo");
        }

        logger.info("Obtendo usuários que o usuário com ID {} está seguindo", userId);
        List<Follow> follows = followRepository.findByFollowerId(userId);
        if (follows.isEmpty()) {
            logger.info("Usuário com ID {} não está seguindo ninguém", userId);
            return List.of();
        }

        List<FollowListDto> following = follows.stream()
                .map(follow -> new FollowListDto(follow.getFollowed().getId(), follow.getFollowed().getEmail(), follow.getFollowed().getNickname()))
                .toList();

        logger.info("Usuário com ID {} está seguindo {} usuários", userId, following.size());
        return following;


    }

    public long getFollowersCount(User user) {
        return followRepository.countByFollowed(user);
    }

    public long getFollowingCount(User user) {
        return followRepository.countByFollower(user);
    }


}
