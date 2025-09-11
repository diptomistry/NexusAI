package com.example.ai_assistant_backend.repository;

import com.example.ai_assistant_backend.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // Find conversations by user ID
    List<Conversation> findByUserIdOrderByUpdatedAtDesc(String userId);

    // Find conversations by user ID and assistant ID
    List<Conversation> findByUserIdAndAssistantIdOrderByUpdatedAtDesc(String userId, String assistantId);

    // Find conversation with messages
    @Query("SELECT c FROM Conversation c LEFT JOIN FETCH c.messages WHERE c.id = :conversationId")
    Conversation findByIdWithMessages(@Param("conversationId") Long conversationId);
}
