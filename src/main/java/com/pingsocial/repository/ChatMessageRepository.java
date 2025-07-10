package com.pingsocial.repository;

import com.pingsocial.models.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    void deleteBySenderInAndRecipientIn(List<String> senders, List<String> recipients);

    @Query("SELECT COUNT(c) FROM chat_message c WHERE (c.sender = :userId AND c.recipient = :userId2) OR (c.sender = :userId2 AND c.recipient = :userId)")
    long countMessagesBetweenUsers(String userId, String userId2);

    List<ChatMessage> findBySenderInAndRecipientInOrderByTimestampAsc(List<String> senders, List<String> recipients, Pageable pageable);
}
