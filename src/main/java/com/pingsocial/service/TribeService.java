package com.pingsocial.service;

import com.pingsocial.exception.TribeNotFoundException;
import com.pingsocial.exception.UserAlreadyInTribeException;
import com.pingsocial.exception.UserNotFoundException;
import com.pingsocial.models.Tribe;
import com.pingsocial.models.User;
import com.pingsocial.repository.TribeRepository;
import com.pingsocial.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TribeService {

    private static final Logger logger = LoggerFactory.getLogger(TribeService.class);

    private final TribeRepository tribeRepository;
    private final UserRepository userRepository;

    public TribeService(TribeRepository tribeRepository, UserRepository userRepository) {
        this.tribeRepository = tribeRepository;
        this.userRepository = userRepository;
    }

    /**
     * Adiciona um usuário a uma tribo.
     *
     * @param userId  ID do usuário a ser adicionado
     * @param tribeId ID da tribo onde o usuário será adicionado
     * @throws UserNotFoundException       se o usuário não for encontrado
     * @throws TribeNotFoundException      se a tribo não for encontrada
     * @throws UserAlreadyInTribeException se o usuário já for membro da tribo
     * @throws IllegalArgumentException    se userId ou tribeId forem nulos
     */
    @Transactional
    public void joinTribe(Long userId, Long tribeId) {
        // Validação de entrada
        if (userId == null || tribeId == null) {
            logger.error("Tentativa de adicionar usuário a tribo com ID nulo. userId={}, tribeId={}", userId, tribeId);
            throw new IllegalArgumentException("IDs de usuário e tribo não podem ser nulos");
        }

        logger.info("Iniciando processo de adição do usuário {} à tribo {}", userId, tribeId);

        // Busca o usuário
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Usuário não encontrado com ID: {}", userId);
                    return new UserNotFoundException(userId);
                });

        // Busca a tribo
        Tribe tribe = tribeRepository.findById(tribeId)
                .orElseThrow(() -> {
                    logger.error("Tribo não encontrada com ID: {}", tribeId);
                    return new TribeNotFoundException(tribeId);
                });

        // Verifica se o usuário já é membro da tribo
        if (tribe.getMembers().contains(user)) {
            logger.warn("Usuário {} já é membro da tribo {}", userId, tribeId);
            throw new UserAlreadyInTribeException(userId, tribeId);
        }

        // Adiciona o usuário à tribo e a tribo ao usuário
        tribe.getMembers().add(user);
        user.getTribes().add(tribe);

        // Salva as alterações
        tribeRepository.save(tribe);
        userRepository.save(user);

        logger.info("Usuário {} adicionado com sucesso à tribo {}", userId, tribeId);
    }

    /**
     * Remove um usuário de uma tribo.
     *
     * @param userId  ID do usuário a ser removido
     * @param tribeId ID da tribo de onde o usuário será removido
     * @throws UserNotFoundException    se o usuário não for encontrado
     * @throws TribeNotFoundException   se a tribo não for encontrada
     * @throws IllegalArgumentException se userId ou tribeId forem nulos
     */
    @Transactional
    public void leaveTribe(Long userId, Long tribeId) {
        if (userId == null || tribeId == null) {
            logger.error("Tentativa de remover usuário de tribo com ID nulo. userId={}, tribeId={}", userId, tribeId);
            throw new IllegalArgumentException("IDs de usuário e tribo não podem ser nulos");
        }

        logger.info("Iniciando processo de remoção do usuário {} da tribo {}", userId, tribeId);


        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Usuário não encontrado com ID: {}", userId);
                    return new UserNotFoundException(userId);
                });


        Tribe tribe = tribeRepository.findById(tribeId)
                .orElseThrow(() -> {
                    logger.error("Tribo não encontrada com ID: {}", tribeId);
                    return new TribeNotFoundException(tribeId);
                });


        if (!tribe.getMembers().contains(user)) {
            logger.warn("Usuário {} não é membro da tribo {}", userId, tribeId);
            return;
        }

        tribe.getMembers().remove(user);
        user.getTribes().remove(tribe);

        tribeRepository.save(tribe);
        userRepository.save(user);

        logger.info("Usuário {} removido com sucesso da tribo {}", userId, tribeId);
    }

    /**
     * Obtém todas as tribos das quais um usuário é membro.
     *
     * @param userId ID do usuário
     * @return Conjunto de tribos das quais o usuário é membro
     * @throws UserNotFoundException    se o usuário não for encontrado
     * @throws IllegalArgumentException se userId for nulo
     */
    @Transactional(readOnly = true)
    public Set<Tribe> getUserTribes(Long userId) {
        if (userId == null) {
            logger.error("Tentativa de obter tribos de usuário com ID nulo");
            throw new IllegalArgumentException("ID de usuário não pode ser nulo");
        }

        logger.info("Iniciando busca de tribos para o usuário {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Usuário não encontrado com ID: {}", userId);
                    return new UserNotFoundException(userId);
                });

        Set<Tribe> tribes = user.getTribes();
        logger.info("Encontradas {} tribos para o usuário {}", tribes.size(), userId);

        return tribes;
    }

    /**
     * Obtém todos os membros de uma tribo.
     *
     * @param tribeId ID da tribo
     * @return Conjunto de usuários que são membros da tribo
     * @throws TribeNotFoundException   se a tribo não for encontrada
     * @throws IllegalArgumentException se tribeId for nulo
     */
    @Transactional(readOnly = true)
    public Set<User> getTribeMembers(Long tribeId) {
        if (tribeId == null) {
            logger.error("Tentativa de obter membros de tribo com ID nulo");
            throw new IllegalArgumentException("ID de tribo não pode ser nulo");
        }

        logger.info("Iniciando busca de membros para a tribo {}", tribeId);

        Tribe tribe = tribeRepository.findById(tribeId)
                .orElseThrow(() -> {
                    logger.error("Tribo não encontrada com ID: {}", tribeId);
                    return new TribeNotFoundException(tribeId);
                });

        Set<User> members = tribe.getMembers();
        logger.info("Encontrados {} membros para a tribo {}", members.size(), tribeId);

        return members;
    }

    /**
     * Lista todas as tribos disponíveis.
     *
     * @return Conjunto de todas as tribos
     * @throws IllegalArgumentException se não houver tribos disponíveis
     * @throws TribeNotFoundException   se não houver tribos cadastradas
     * @throws Exception                se ocorrer um erro inesperado ao buscar as tribos
     */
    @Transactional(readOnly = true)
    public List<Tribe> listAllTribes() {
        logger.info("Iniciando busca de todas as tribos");

        List<Tribe> tribes = tribeRepository.findAll();

        if (tribes.isEmpty()) {
            logger.error("Nenhuma tribo encontrada");
            throw new TribeNotFoundException("Nenhuma tribo cadastrada");
        }

        logger.info("Encontradas {} tribos", tribes.size());
        return tribes;
    }

    /**
     * Verifica se o usuário autenticado é membro de alguma tribo.
     *
     * @return true se o usuário tiver pelo menos uma tribo, false caso contrário
     */
    public boolean userHasAnyTribe() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new com.pingsocial.exception.UserNotFoundException(email));
        return user.getTribes() != null && !user.getTribes().isEmpty();
    }

    public Set<Long> getTribeIdsByUserId(Long userId) {
        if (userId == null) {
            logger.error("Tentativa de obter IDs de tribos com ID de usuário nulo");
            throw new IllegalArgumentException("ID de usuário não pode ser nulo");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Usuário não encontrado com ID: {}", userId);
                    return new UserNotFoundException(userId);
                });

        Set<Long> tribeIds = user.getTribes().stream()
                .map(Tribe::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        logger.info("Encontrados {} IDs de tribos para o usuário {}", tribeIds.size(), userId);
        return tribeIds;
    }

    public Tribe getTribeById(Long tribeId) {
        if (tribeId == null) {
            throw new IllegalArgumentException("ID da tribo não pode ser nulo");
        }
        return tribeRepository.findById(tribeId).orElse(null);
    }




}
