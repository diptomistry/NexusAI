package com.example.ai_assistant_backend.repository;

import com.example.ai_assistant_backend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Find messages by conversation ID ordered by creation time
    List<Message> findByConversationIdOrderByCreatedAt(Long conversationId);
}
