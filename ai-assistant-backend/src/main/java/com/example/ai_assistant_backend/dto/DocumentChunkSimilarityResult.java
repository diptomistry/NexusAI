package com.example.ai_assistant_backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for document chunk similarity search results
 * Demonstrates advanced DTO patterns for complex queries
 */
public class DocumentChunkSimilarityResult {

    private UUID id;
    private Long documentId;
    private String chunkText;
    private Integer chunkIndex;
    private Integer chunkTokens;
    private Double similarity;
    private String documentFileName;
    private LocalDateTime createdAt;

    // Constructors
    public DocumentChunkSimilarityResult() {
    }

    public DocumentChunkSimilarityResult(UUID id, Long documentId, String chunkText,
            Integer chunkIndex, Integer chunkTokens, Double similarity) {
        this.id = id;
        this.documentId = documentId;
        this.chunkText = chunkText;
        this.chunkIndex = chunkIndex;
        this.chunkTokens = chunkTokens;
        this.similarity = similarity;
    }

    public DocumentChunkSimilarityResult(UUID id, Long documentId, String chunkText,
            Integer chunkIndex, Integer chunkTokens, Double similarity,
            String documentFileName, LocalDateTime createdAt) {
        this(id, documentId, chunkText, chunkIndex, chunkTokens, similarity);
        this.documentFileName = documentFileName;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getChunkText() {
        return chunkText;
    }

    public void setChunkText(String chunkText) {
        this.chunkText = chunkText;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public Integer getChunkTokens() {
        return chunkTokens;
    }

    public void setChunkTokens(Integer chunkTokens) {
        this.chunkTokens = chunkTokens;
    }

    public Double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Double similarity) {
        this.similarity = similarity;
    }

    public String getDocumentFileName() {
        return documentFileName;
    }

    public void setDocumentFileName(String documentFileName) {
        this.documentFileName = documentFileName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Utility methods
    public boolean isHighSimilarity() {
        return similarity != null && similarity > 0.8;
    }

    public boolean isMediumSimilarity() {
        return similarity != null && similarity > 0.6 && similarity <= 0.8;
    }

    public String getTruncatedText(int maxLength) {
        if (chunkText == null)
            return "";
        if (chunkText.length() <= maxLength)
            return chunkText;
        return chunkText.substring(0, maxLength) + "...";
    }

    @Override
    public String toString() {
        return "DocumentChunkSimilarityResult{" +
                "id=" + id +
                ", documentId=" + documentId +
                ", chunkIndex=" + chunkIndex +
                ", similarity=" + similarity +
                ", textLength=" + (chunkText != null ? chunkText.length() : 0) +
                '}';
    }
}
