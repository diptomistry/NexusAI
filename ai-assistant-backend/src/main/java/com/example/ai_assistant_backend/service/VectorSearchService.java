package com.example.ai_assistant_backend.service;

import com.example.ai_assistant_backend.dto.DocumentChunkSimilarityResult;
import com.example.ai_assistant_backend.model.DocumentChunk;
import com.example.ai_assistant_backend.repository.DocumentChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Advanced Vector Search Service
 * Demonstrates enterprise patterns for vector similarity search and data
 * processing
 */
@Service
@Transactional
public class VectorSearchService {

    private static final Logger logger = LoggerFactory.getLogger(VectorSearchService.class);

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Autowired
    private EmbeddingService embeddingService;

    /**
     * Semantic search with advanced filtering and ranking
     */
    @Cacheable(value = "similaritySearch", key = "#query.hashCode() + '_' + #userId + '_' + #assistantId")
    public List<DocumentChunkSimilarityResult> semanticSearch(
            String query,
            UUID userId,
            String assistantId,
            Double threshold,
            Integer limit) {

        try {
            // Generate embedding for the query
            float[] queryEmbedding = embeddingService.generateEmbedding(query);
            String embeddingVector = convertFloatArrayToVectorString(queryEmbedding);

            // Set default values
            threshold = threshold != null ? threshold : 0.7;
            limit = limit != null ? limit : 10;

            logger.info("Performing semantic search for user {} assistant {} with threshold {} limit {}",
                    userId, assistantId, threshold, limit);

            // Execute similarity search
            List<Object[]> results = documentChunkRepository.findSimilarChunksNative(
                    embeddingVector, userId, assistantId, threshold, limit);

            // Convert to DTOs
            List<DocumentChunkSimilarityResult> searchResults = convertToSimilarityResults(results);

            logger.info("Found {} similar chunks for query", searchResults.size());

            return searchResults;

        } catch (Exception e) {
            logger.error("Error performing semantic search: {}", e.getMessage(), e);
            throw new RuntimeException("Semantic search failed: " + e.getMessage(), e);
        }
    }

    /**
     * Advanced semantic search with document filtering
     */
    public List<DocumentChunkSimilarityResult> semanticSearchWithDocumentFilter(
            String query,
            UUID userId,
            String assistantId,
            List<Long> documentIds,
            Double threshold,
            Integer limit) {

        try {
            // Generate embedding for the query
            float[] queryEmbedding = embeddingService.generateEmbedding(query);
            String embeddingVector = convertFloatArrayToVectorString(queryEmbedding);

            // Convert document IDs to PostgreSQL array format
            String documentIdsArray = documentIds != null && !documentIds.isEmpty()
                    ? "{" + String.join(",", documentIds.stream().map(String::valueOf).toList()) + "}"
                    : null;

            // Set default values
            threshold = threshold != null ? threshold : 0.7;
            limit = limit != null ? limit : 10;

            logger.info("Performing filtered semantic search for user {} assistant {} with {} documents, threshold {}, limit {}",
                    userId, assistantId, documentIds != null ? documentIds.size() : "all", threshold, limit);

            // Execute similarity search with document filter
            List<Object[]> results = documentChunkRepository.findSimilarChunksWithDocumentFilter(
                    embeddingVector, userId, assistantId, documentIdsArray, threshold, limit);

            // Convert to DTOs
            return convertToSimilarityResults(results);

        } catch (Exception e) {
            logger.error("Error performing filtered semantic search: {}", e.getMessage(), e);
            throw new RuntimeException("Filtered semantic search failed: " + e.getMessage(), e);
        }
    }

    /**
     * Multi-query semantic search (search with multiple related queries)
     */
    public List<DocumentChunkSimilarityResult> multiQuerySemanticSearch(
            List<String> queries,
            UUID userId,
            String assistantId,
            Double threshold,
            Integer limitPerQuery) {

        List<DocumentChunkSimilarityResult> allResults = new ArrayList<>();

        for (String query : queries) {
            List<DocumentChunkSimilarityResult> queryResults = semanticSearch(
                    query, userId, assistantId, threshold, limitPerQuery);
            allResults.addAll(queryResults);
        }

        // Remove duplicates and sort by similarity
        return allResults.stream()
                .collect(Collectors.toMap(
                        DocumentChunkSimilarityResult::getId,
                        r -> r,
                        (existing, replacement) -> existing.getSimilarity() > replacement.getSimilarity() ? existing
                                : replacement))
                .values()
                .stream()
                .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
                .collect(Collectors.toList());
    }

    /**
     * Get contextual chunks around a specific chunk
     */
    public List<DocumentChunk> getContextualChunks(UUID chunkId, int contextWindow) {
        DocumentChunk targetChunk = documentChunkRepository.findById(chunkId).orElse(null);
        if (targetChunk == null) {
            return new ArrayList<>();
        }

        List<DocumentChunk> allChunks = documentChunkRepository.findByDocumentIdOrderByChunkIndex(
                targetChunk.getDocument().getId());

        int targetIndex = -1;
        for (int i = 0; i < allChunks.size(); i++) {
            if (allChunks.get(i).getId().equals(chunkId)) {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex == -1) {
            return List.of(targetChunk);
        }

        int start = Math.max(0, targetIndex - contextWindow);
        int end = Math.min(allChunks.size(), targetIndex + contextWindow + 1);

        return allChunks.subList(start, end);
    }

    /**
     * Get chunk statistics for monitoring
     */
    public ChunkStatistics getChunkStatistics(UUID userId, String assistantId) {
        Object[] stats = documentChunkRepository.getChunkStatistics(userId, assistantId);

        if (stats == null || stats.length < 6) {
            return new ChunkStatistics(0L, 0.0, 0, 0, 0L, 0L);
        }

        return new ChunkStatistics(
                ((Number) stats[0]).longValue(), // total_chunks
                ((Number) stats[1]).doubleValue(), // avg_tokens
                ((Number) stats[2]).intValue(), // min_tokens
                ((Number) stats[3]).intValue(), // max_tokens
                ((Number) stats[4]).longValue(), // unique_documents
                ((Number) stats[5]).longValue() // chunks_with_embeddings
        );
    }

    /**
     * Find duplicate chunks for deduplication
     */
    public List<DuplicateChunkPair> findDuplicateChunks(UUID userId, String assistantId, Double similarityThreshold) {
        similarityThreshold = similarityThreshold != null ? similarityThreshold : 0.95;

        List<Object[]> duplicates = documentChunkRepository.findSimilarChunkPairs(
                userId, assistantId, similarityThreshold, 100);

        return duplicates.stream()
                .map(row -> new DuplicateChunkPair(
                        (UUID) row[0],
                        (UUID) row[1],
                        ((Number) row[2]).doubleValue()))
                .collect(Collectors.toList());
    }

    /**
     * Get recent chunks for monitoring
     */
    public List<DocumentChunk> getRecentChunks(UUID userId, String assistantId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return documentChunkRepository.findRecentChunks(userId, assistantId, pageable);
    }

    /**
     * Convert float array to PostgreSQL vector string format
     */
    private String convertFloatArrayToVectorString(float[] array) {
        if (array == null || array.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(array[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Convert native query results to DTOs
     */
    private List<DocumentChunkSimilarityResult> convertToSimilarityResults(List<Object[]> results) {
        return results.stream().map(row -> {
            DocumentChunkSimilarityResult result = new DocumentChunkSimilarityResult();

            result.setId((UUID) row[0]);
            result.setDocumentId(((Number) row[1]).longValue());
            result.setChunkText((String) row[2]);
            result.setChunkIndex((Integer) row[3]);
            result.setChunkTokens((Integer) row[4]);
            result.setSimilarity(((Number) row[5]).doubleValue());
            result.setDocumentFileName((String) row[6]);

            // Handle timestamp conversion
            if (row[7] instanceof Timestamp) {
                result.setCreatedAt(((Timestamp) row[7]).toLocalDateTime());
            }

            return result;
        }).collect(Collectors.toList());
    }

    // Inner classes for statistics and duplicate detection
    public static class ChunkStatistics {
        private final Long totalChunks;
        private final Double avgTokens;
        private final Integer minTokens;
        private final Integer maxTokens;
        private final Long uniqueDocuments;
        private final Long chunksWithEmbeddings;

        public ChunkStatistics(Long totalChunks, Double avgTokens, Integer minTokens,
                Integer maxTokens, Long uniqueDocuments, Long chunksWithEmbeddings) {
            this.totalChunks = totalChunks;
            this.avgTokens = avgTokens;
            this.minTokens = minTokens;
            this.maxTokens = maxTokens;
            this.uniqueDocuments = uniqueDocuments;
            this.chunksWithEmbeddings = chunksWithEmbeddings;
        }

        // Getters
        public Long getTotalChunks() {
            return totalChunks;
        }

        public Double getAvgTokens() {
            return avgTokens;
        }

        public Integer getMinTokens() {
            return minTokens;
        }

        public Integer getMaxTokens() {
            return maxTokens;
        }

        public Long getUniqueDocuments() {
            return uniqueDocuments;
        }

        public Long getChunksWithEmbeddings() {
            return chunksWithEmbeddings;
        }

        public double getEmbeddingCoverage() {
            return totalChunks > 0 ? (double) chunksWithEmbeddings / totalChunks : 0.0;
        }
    }

    public static class DuplicateChunkPair {
        private final UUID chunkId1;
        private final UUID chunkId2;
        private final Double similarity;

        public DuplicateChunkPair(UUID chunkId1, UUID chunkId2, Double similarity) {
            this.chunkId1 = chunkId1;
            this.chunkId2 = chunkId2;
            this.similarity = similarity;
        }

        // Getters
        public UUID getChunkId1() {
            return chunkId1;
        }

        public UUID getChunkId2() {
            return chunkId2;
        }

        public Double getSimilarity() {
            return similarity;
        }
    }
}
