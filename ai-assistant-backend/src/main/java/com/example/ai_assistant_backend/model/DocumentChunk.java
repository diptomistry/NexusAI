package com.example.ai_assistant_backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Advanced DocumentChunk entity for vector-based document processing
 * Demonstrates enterprise-level JPA usage with custom types and relationships
 */
@Entity
@Table(name = "document_chunks", indexes = {
        @Index(name = "idx_document_chunks_user_assistant", columnList = "user_id, assistant_id"),
        @Index(name = "idx_document_chunks_document_id", columnList = "document_id"),
        @Index(name = "idx_document_chunks_created_at", columnList = "created_at")
})
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "chunk_text", nullable = false, columnDefinition = "TEXT")
    private String chunkText;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(name = "chunk_tokens", nullable = false)
    private Integer chunkTokens;

    // Vector embedding stored as PostgreSQL vector type
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private float[] embedding;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "assistant_id")
    private String assistantId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public DocumentChunk() {
    }

    public DocumentChunk(Document document, String chunkText, Integer chunkIndex,
            Integer chunkTokens, float[] embedding, UUID userId, String assistantId) {
        this.document = document;
        this.chunkText = chunkText;
        this.chunkIndex = chunkIndex;
        this.chunkTokens = chunkTokens;
        this.embedding = embedding;
        this.userId = userId;
        this.assistantId = assistantId;
    }

    // Getters and Setters with validation
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getChunkText() {
        return chunkText;
    }

    public void setChunkText(String chunkText) {
        if (chunkText == null || chunkText.trim().isEmpty()) {
            throw new IllegalArgumentException("Chunk text cannot be null or empty");
        }
        this.chunkText = chunkText;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        if (chunkIndex == null || chunkIndex < 0) {
            throw new IllegalArgumentException("Chunk index must be non-negative");
        }
        this.chunkIndex = chunkIndex;
    }

    public Integer getChunkTokens() {
        return chunkTokens;
    }

    public void setChunkTokens(Integer chunkTokens) {
        if (chunkTokens == null || chunkTokens <= 0) {
            throw new IllegalArgumentException("Chunk tokens must be positive");
        }
        this.chunkTokens = chunkTokens;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getAssistantId() {
        return assistantId;
    }

    public void setAssistantId(String assistantId) {
        this.assistantId = assistantId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Business logic methods
    public boolean hasValidEmbedding() {
        return embedding != null && embedding.length > 0;
    }

    public double getTextDensity() {
        return chunkText != null ? (double) chunkTokens / chunkText.length() : 0.0;
    }

    // Equals and HashCode
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        DocumentChunk that = (DocumentChunk) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DocumentChunk{" +
                "id=" + id +
                ", chunkIndex=" + chunkIndex +
                ", chunkTokens=" + chunkTokens +
                ", textLength=" + (chunkText != null ? chunkText.length() : 0) +
                ", hasEmbedding=" + hasValidEmbedding() +
                '}';
    }
}
