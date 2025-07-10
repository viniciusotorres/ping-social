package com.pingsocial.controller;

import com.pingsocial.models.ChatMessage;
import com.pingsocial.repository.ChatMessageRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    private static final Logger logger = LoggerFactory.getLogger(ChatRestController.class);
    private final ChatMessageRepository chatMessageRepository;

    public ChatRestController(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * Endpoint para obter o histórico de mensagens entre dois usuários.
     */
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getHistory(
            @RequestParam(value = "userId") @Email String userId,
            @RequestParam(value = "userId2") @Email String userId2,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        logger.info("Buscando histórico de mensagens entre {} e {}", userId, userId2);

        try {
            Pageable pageable = PageRequest.of(page, size);
            var messages = chatMessageRepository.findBySenderInAndRecipientInOrderByTimestampAsc(
                    List.of(userId, userId2),
                    List.of(userId, userId2),
                    pageable
            );

            logger.info("Foram encontradas {} mensagens entre {} e {}", messages.size(), userId, userId2);
            return ResponseEntity.ok(messages);

        } catch (Exception e) {
            logger.error("Erro ao buscar o histórico de mensagens entre {} e {}", userId, userId2, e);
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Endpoint para limpar o histórico de mensagens entre dois usuários.
     */

    @DeleteMapping("/clear")
    @Transactional
    public ResponseEntity<String> clearConversation(
            @RequestParam(value = "userId") @Email String userId,
            @RequestParam(value = "userId2") @Email String userId2
    ) {

        logger.info("Requisição para limpar o histórico de mensagens entre {} e {}", userId, userId2);

        try {
            long messageCount = chatMessageRepository.countMessagesBetweenUsers(userId, userId2);
            if (messageCount == 0) {
                logger.warn("Não foram encontradas mensagens entre {} e {}", userId, userId2);
                return ResponseEntity.status(404).body("Nenhuma mensagem encontrada para excluir.");
            }

            chatMessageRepository.deleteBySenderInAndRecipientIn(List.of(userId, userId2), List.of(userId, userId2));

            logger.info("Histórico de mensagens entre {} e {} foi limpo com sucesso.", userId, userId2);
            return ResponseEntity.ok("Conversa limpa com sucesso.");

        } catch (Exception e) {
            logger.error("Erro ao limpar o histórico de mensagens entre {} e {}", userId, userId2, e);
            return ResponseEntity.status(500).body("Erro ao limpar a conversa.");
        }
    }
}
