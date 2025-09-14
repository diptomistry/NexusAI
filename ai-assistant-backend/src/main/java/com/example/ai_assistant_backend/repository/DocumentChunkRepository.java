package com.example.ai_assistant_backend.repository;

import com.example.ai_assistant_backend.dto.DocumentChunkSimilarityResult;
import com.example.ai_assistant_backend.model.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Advanced DocumentChunk repository with vector similarity search capabilities
 * Demonstrates enterprise-level repository patterns with native SQL queries
 */
@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {

    /**
     * Find chunks by document ID with ordering
     */
    @Query("SELECT dc FROM DocumentChunk dc WHERE dc.document.id = :documentId ORDER BY dc.chunkIndex ASC")
    List<DocumentChunk> findByDocumentIdOrderByChunkIndex(@Param("documentId") Long documentId);

    /**
     * Find chunks by user and assistant
     */
    @Query("SELECT dc FROM DocumentChunk dc WHERE dc.userId = :userId AND dc.assistantId = :assistantId ORDER BY dc.createdAt DESC")
    List<DocumentChunk> findByUserIdAndAssistantIdOrderByCreatedAt(
            @Param("userId") UUID userId,
            @Param("assistantId") String assistantId);

    /**
     * Vector similarity search using pgvector
     * This is the core RAG functionality
     */
    @Query(value = """
            SELECT
                dc.id,
                dc.document_id,
                dc.chunk_text,
                dc.chunk_index,
                dc.chunk_tokens,
                (dc.embedding <=> CAST(:queryEmbedding AS vector)) * -1 + 1 AS similarity,
                d.original_file_name,
                dc.created_at
            FROM document_chunks dc
            JOIN documents d ON dc.document_id = d.id
            WHERE dc.user_id = :userId
            AND (:assistantId IS NULL OR dc.assistant_id = :assistantId)
            AND dc.embedding IS NOT NULL
            AND (dc.embedding <=> CAST(:queryEmbedding AS vector)) < (1 - :threshold)
            ORDER BY dc.embedding <=> CAST(:queryEmbedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findSimilarChunksNative(
            @Param("queryEmbedding") String queryEmbedding,
            @Param("userId") UUID userId,
            @Param("assistantId") String assistantId,
            @Param("threshold") Double threshold,
            @Param("limit") Integer limit);

    /**
     * Advanced similarity search with document filtering
     */
    @Query(value = """
            SELECT
                dc.id,
                dc.document_id,
                dc.chunk_text,
                dc.chunk_index,
                dc.chunk_tokens,
                (dc.embedding <=> CAST(:queryEmbedding AS vector)) * -1 + 1 AS similarity,
                d.original_file_name,
                dc.created_at
            FROM document_chunks dc
            JOIN documents d ON dc.document_id = d.id
            WHERE dc.user_id = :userId
            AND (:assistantId IS NULL OR dc.assistant_id = :assistantId)
            AND (:documentIds IS NULL OR dc.document_id = ANY(CAST(:documentIds AS bigint[])))
            AND dc.embedding IS NOT NULL
            AND (dc.embedding <=> CAST(:queryEmbedding AS vector)) < (1 - :threshold)
            ORDER BY dc.embedding <=> CAST(:queryEmbedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findSimilarChunksWithDocumentFilter(
            @Param("queryEmbedding") String queryEmbedding,
            @Param("userId") UUID userId,
            @Param("assistantId") String assistantId,
            @Param("documentIds") String documentIds, // PostgreSQL array format
            @Param("threshold") Double threshold,
            @Param("limit") Integer limit);

    /**
     * Get chunk statistics for performance monitoring
     */
    @Query(value = """
            SELECT
                COUNT(*) as total_chunks,
                AVG(chunk_tokens) as avg_tokens,
                MIN(chunk_tokens) as min_tokens,
                MAX(chunk_tokens) as max_tokens,
                COUNT(DISTINCT document_id) as unique_documents,
                COUNT(CASE WHEN embedding IS NOT NULL THEN 1 END) as chunks_with_embeddings
            FROM document_chunks
            WHERE user_id = :userId
            AND (:assistantId IS NULL OR assistant_id = :assistantId)
            """, nativeQuery = true)
    Object[] getChunkStatistics(@Param("userId") UUID userId, @Param("assistantId") String assistantId);

    /**
     * Find chunks without embeddings for processing
     */
    @Query("SELECT dc FROM DocumentChunk dc WHERE dc.embedding IS NULL ORDER BY dc.createdAt ASC")
    List<DocumentChunk> findChunksWithoutEmbeddings();

    /**
     * Find chunks without embeddings for specific user/assistant
     */
    @Query("SELECT dc FROM DocumentChunk dc WHERE dc.userId = :userId AND dc.assistantId = :assistantId AND dc.embedding IS NULL ORDER BY dc.createdAt ASC")
    List<DocumentChunk> findChunksWithoutEmbeddingsByUserAndAssistant(
            @Param("userId") UUID userId,
            @Param("assistantId") String assistantId);

    /**
     * Count chunks by document
     */
    @Query("SELECT COUNT(dc) FROM DocumentChunk dc WHERE dc.document.id = :documentId")
    Long countByDocumentId(@Param("documentId") Long documentId);

    /**
     * Count chunks by user and assistant
     */
    @Query("SELECT COUNT(dc) FROM DocumentChunk dc WHERE dc.userId = :userId AND dc.assistantId = :assistantId")
    Long countByUserIdAndAssistantId(@Param("userId") UUID userId, @Param("assistantId") String assistantId);

    /**
     * Delete chunks by document ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM DocumentChunk dc WHERE dc.document.id = :documentId")
    void deleteByDocumentId(@Param("documentId") Long documentId);

    /**
     * Update embedding for a chunk
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE document_chunks SET embedding = CAST(:embedding AS vector) WHERE id = :chunkId", nativeQuery = true)
    void updateEmbedding(@Param("chunkId") UUID chunkId, @Param("embedding") float[] embedding);

    /**
     * Batch update embeddings
     */
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE document_chunks
            SET embedding = CAST(:embedding AS vector)
            WHERE id = ANY(CAST(:chunkIds AS uuid[]))
            """, nativeQuery = true)
    void batchUpdateEmbeddings(@Param("chunkIds") String chunkIds, @Param("embedding") String embedding);

    /**
     * Find chunks with high similarity to each other (for deduplication)
     */
    @Query(value = """
            SELECT DISTINCT dc1.id, dc2.id, (dc1.embedding <=> dc2.embedding) * -1 + 1 AS similarity
            FROM document_chunks dc1
            JOIN document_chunks dc2 ON dc1.id < dc2.id
            WHERE dc1.user_id = :userId
            AND dc2.user_id = :userId
            AND dc1.assistant_id = :assistantId
            AND dc2.assistant_id = :assistantId
            AND dc1.embedding IS NOT NULL
            AND dc2.embedding IS NOT NULL
            AND (dc1.embedding <=> dc2.embedding) * -1 + 1 > :similarityThreshold
            ORDER BY similarity DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findSimilarChunkPairs(
            @Param("userId") UUID userId,
            @Param("assistantId") String assistantId,
            @Param("similarityThreshold") Double similarityThreshold,
            @Param("limit") Integer limit);

    /**
     * Get most recent chunks for a user/assistant
     */
    @Query("SELECT dc FROM DocumentChunk dc WHERE dc.userId = :userId AND dc.assistantId = :assistantId ORDER BY dc.createdAt DESC")
    List<DocumentChunk> findRecentChunks(
            @Param("userId") UUID userId,
            @Param("assistantId") String assistantId,
            org.springframework.data.domain.Pageable pageable);
}
