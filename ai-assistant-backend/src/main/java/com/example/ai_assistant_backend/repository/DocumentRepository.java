package com.example.ai_assistant_backend.repository;

import com.example.ai_assistant_backend.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Document> findByUserIdAndAssistantIdOrderByCreatedAtDesc(String userId, String assistantId);

    @Query("SELECT d FROM Document d WHERE d.userId = :userId AND " +
            "(d.assistantId = :assistantId OR d.assistantId IS NULL) " +
            "ORDER BY d.createdAt DESC")
    List<Document> findDocumentsForAssistant(@Param("userId") String userId,
            @Param("assistantId") String assistantId);

    @Query("SELECT d.extractedText FROM Document d WHERE d.userId = :userId AND " +
            "(d.assistantId = :assistantId OR d.assistantId IS NULL)")
    List<String> findExtractedTextForAssistant(@Param("userId") String userId,
            @Param("assistantId") String assistantId);

    long countByUserId(String userId);
}
