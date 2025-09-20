package com.example.ai_assistant_backend.controller;

import com.example.ai_assistant_backend.dto.DocumentChunkSimilarityResult;
import com.example.ai_assistant_backend.dto.DocumentUploadResponse;
import com.example.ai_assistant_backend.model.Document;
import com.example.ai_assistant_backend.model.DocumentChunk;
import com.example.ai_assistant_backend.repository.DocumentRepository;
import com.example.ai_assistant_backend.repository.DocumentChunkRepository;
import com.example.ai_assistant_backend.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.UUID;

/**
 * Enhanced Vector-Powered Document Controller
 * Demonstrates advanced RAG integration with comprehensive document processing
 */
@RestController
@RequestMapping("/api/vector-documents")
@CrossOrigin(origins = "http://localhost:3000")
public class VectorDocumentController {

    private static final Logger logger = LoggerFactory.getLogger(VectorDocumentController.class);

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentChunkingService chunkingService;

    @Autowired
    private VectorSearchService vectorSearchService;

    @Autowired
    private RetrievalAugmentedGenerationService ragService;

    @Autowired
    private VectorPerformanceMonitoringService performanceMonitoringService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    /**
     * Upload document with advanced vector processing
     */
    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam("assistantId") String assistantId) {

        Instant start = Instant.now();
        VectorPerformanceMonitoringService.PerformanceTimer timer = performanceMonitoringService
                .startTimer("document_upload");

        try {
            logger.info("Starting enhanced document upload for user {} assistant {}", userId, assistantId);

            // Upload document using existing service
            DocumentUploadResponse response = documentService.uploadDocument(file, userId, assistantId);

            if (response.isSuccess()) {
                // Get the uploaded document
                Document document = documentRepository.findById(response.getId()).orElse(null);

                if (document != null) {
                    // Process document asynchronously with chunking and embedding generation
                    chunkingService.processDocumentAsync(document)
                            .thenAccept(chunks -> {
                                logger.info("Successfully processed document {} with {} chunks",
                                        document.getId(), chunks.size());

                                performanceMonitoringService.recordChunkingOperation(
                                        document.getFileType(),
                                        Duration.between(start, Instant.now()),
                                        document.getExtractedText() != null ? document.getExtractedText().length() : 0,
                                        chunks.size(),
                                        true);
                            })
                            .exceptionally(throwable -> {
                                logger.error("Failed to process document {}: {}", document.getId(),
                                        throwable.getMessage());

                                performanceMonitoringService.recordChunkingOperation(
                                        document.getFileType(),
                                        Duration.between(start, Instant.now()),
                                        document.getExtractedText() != null ? document.getExtractedText().length() : 0,
                                        0,
                                        false);
                                return null;
                            });
                }
            }

            timer.stopAndRecord(response.isSuccess());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Document upload failed: {}", e.getMessage(), e);
            timer.stopAndRecord(false);
            return ResponseEntity.badRequest()
                    .body(new DocumentUploadResponse("Upload failed: " + e.getMessage(), false));
        }
    }

    /**
     * Semantic search in documents
     */
    @GetMapping("/semantic-search")
    public ResponseEntity<List<DocumentChunkSimilarityResult>> semanticSearch(
            @RequestParam("query") String query,
            @RequestParam("userId") String userId,
            @RequestParam("assistantId") String assistantId,
            @RequestParam(value = "threshold", defaultValue = "0.7") Double threshold,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {

        Instant start = Instant.now();

        try {
            logger.info("Semantic search request: query='{}', user={}, assistant={}",
                    query.substring(0, Math.min(50, query.length())), userId, assistantId);

            List<DocumentChunkSimilarityResult> results = vectorSearchService.semanticSearch(
                    query, UUID.fromString(userId), assistantId, threshold, limit);

            // Record performance metrics
            double avgSimilarity = results.stream()
                    .mapToDouble(DocumentChunkSimilarityResult::getSimilarity)
                    .average()
                    .orElse(0.0);

            performanceMonitoringService.recordVectorSearch(
                    "semantic",
                    Duration.between(start, Instant.now()),
                    results.size(),
                    avgSimilarity,
                    true);

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("Semantic search failed: {}", e.getMessage(), e);

            performanceMonitoringService.recordVectorSearch(
                    "semantic",
                    Duration.between(start, Instant.now()),
                    0,
                    0.0,
                    false);

            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Get RAG context for AI queries
     */
    @PostMapping("/rag-context")
    public ResponseEntity<RetrievalAugmentedGenerationService.RAGContext> getRagContext(
            @RequestBody RAGContextRequest request) {

        Instant start = Instant.now();

        try {
            logger.info("RAG context request for user {} assistant {}", request.getUserId(), request.getAssistantId());

            // Configure RAG
            RetrievalAugmentedGenerationService.RAGConfig config = new RetrievalAugmentedGenerationService.RAGConfig();
            config.setMaxChunks(request.getMaxChunks() != null ? request.getMaxChunks() : 5);
            config.setSimilarityThreshold(request.getThreshold() != null ? request.getThreshold() : 0.7);
            config.setIncludeMetadata(request.getIncludeMetadata() != null ? request.getIncludeMetadata() : true);

            RetrievalAugmentedGenerationService.RAGContext context;

            if (request.getDocumentIds() != null && !request.getDocumentIds().isEmpty()) {
                // Filtered search
                context = ragService.generateContextWithDocumentFilter(
                        request.getQuery(),
                        UUID.fromString(request.getUserId()),
                        request.getAssistantId(),
                        request.getDocumentIds(),
                        config);
            } else {
                // General search
                context = ragService.generateContext(
                        request.getQuery(),
                        UUID.fromString(request.getUserId()),
                        request.getAssistantId(),
                        config);
            }

            // Record performance
            performanceMonitoringService.recordVectorSearch(
                    "rag_context",
                    Duration.between(start, Instant.now()),
                    context.getSourceChunks().size(),
                    context.getMetadata().getAverageSimilarity(),
                    context.hasContext());

            return ResponseEntity.ok(context);

        } catch (Exception e) {
            logger.error("RAG context generation failed: {}", e.getMessage(), e);

            performanceMonitoringService.recordVectorSearch(
                    "rag_context",
                    Duration.between(start, Instant.now()),
                    0,
                    0.0,
                    false);

            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Get chunk statistics for monitoring
     */
    @GetMapping("/chunk-statistics")
    public ResponseEntity<VectorSearchService.ChunkStatistics> getChunkStatistics(
            @RequestParam("userId") String userId,
            @RequestParam("assistantId") String assistantId) {

        try {
            VectorSearchService.ChunkStatistics stats = vectorSearchService.getChunkStatistics(
                    UUID.fromString(userId), assistantId);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            logger.error("Failed to get chunk statistics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Get performance metrics
     */
    @GetMapping("/performance-metrics")
    public ResponseEntity<VectorPerformanceMonitoringService.PerformanceReport> getPerformanceMetrics() {
        try {
            VectorPerformanceMonitoringService.PerformanceReport report = performanceMonitoringService
                    .getPerformanceReport();

            return ResponseEntity.ok(report);

        } catch (Exception e) {
            logger.error("Failed to get performance metrics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Debug endpoint to get all chunks for a document in order
     */
    @GetMapping("/debug-chunks")
    public ResponseEntity<List<Map<String, Object>>> getDebugChunks(
            @RequestParam("documentId") Long documentId) {
        try {
            List<DocumentChunk> chunks = documentChunkRepository.findByDocumentIdOrderByChunkIndex(documentId);

            List<Map<String, Object>> debugInfo = new ArrayList<>();
            for (DocumentChunk chunk : chunks) {
                Map<String, Object> chunkInfo = new HashMap<>();
                chunkInfo.put("chunkIndex", chunk.getChunkIndex());
                chunkInfo.put("textLength", chunk.getChunkText().length());
                chunkInfo.put("textPreview",
                        chunk.getChunkText().substring(0, Math.min(200, chunk.getChunkText().length())));
                chunkInfo.put("hasEmbedding", chunk.hasValidEmbedding());
                debugInfo.add(chunkInfo);
            }

            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            logger.error("Failed to get debug chunks: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Debug endpoint to get all documents and their chunk status for an assistant
     */
    @GetMapping("/debug-assistant-documents")
    public ResponseEntity<Map<String, Object>> getDebugAssistantDocuments(
            @RequestParam("userId") String userId,
            @RequestParam("assistantId") String assistantId) {
        try {
            Map<String, Object> debugInfo = new HashMap<>();

            // Get all documents for this assistant
            List<Document> documents = documentRepository.findByUserIdAndAssistantIdOrderByCreatedAtDesc(userId,
                    assistantId);
            debugInfo.put("totalDocuments", documents.size());

            List<Map<String, Object>> documentInfo = new ArrayList<>();
            for (Document doc : documents) {
                Map<String, Object> docInfo = new HashMap<>();
                docInfo.put("id", doc.getId());
                docInfo.put("fileName", doc.getOriginalFileName());
                docInfo.put("fileSize", doc.getFileSize());
                docInfo.put("createdAt", doc.getCreatedAt());

                // Get chunk count for this document
                List<DocumentChunk> chunks = documentChunkRepository.findByDocumentIdOrderByChunkIndex(doc.getId());
                docInfo.put("chunkCount", chunks.size());

                // Check how many chunks have embeddings
                long chunksWithEmbeddings = chunks.stream()
                        .mapToLong(chunk -> chunk.hasValidEmbedding() ? 1 : 0)
                        .sum();
                docInfo.put("chunksWithEmbeddings", chunksWithEmbeddings);

                documentInfo.add(docInfo);
            }

            debugInfo.put("documents", documentInfo);
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            logger.error("Failed to get debug assistant documents: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Get health status
     */
    @GetMapping("/health")
    public ResponseEntity<VectorPerformanceMonitoringService.HealthStatus> getHealthStatus() {
        try {
            VectorPerformanceMonitoringService.HealthStatus status = performanceMonitoringService.getHealthStatus();

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            logger.error("Failed to get health status: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Request DTO for RAG context
     */
    public static class RAGContextRequest {
        private String query;
        private String userId;
        private String assistantId;
        private List<Long> documentIds;
        private Double threshold;
        private Integer maxChunks;
        private Boolean includeMetadata;

        // Getters and setters
        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getAssistantId() {
            return assistantId;
        }

        public void setAssistantId(String assistantId) {
            this.assistantId = assistantId;
        }

        public List<Long> getDocumentIds() {
            return documentIds;
        }

        public void setDocumentIds(List<Long> documentIds) {
            this.documentIds = documentIds;
        }

        public Double getThreshold() {
            return threshold;
        }

        public void setThreshold(Double threshold) {
            this.threshold = threshold;
        }

        public Integer getMaxChunks() {
            return maxChunks;
        }

        public void setMaxChunks(Integer maxChunks) {
            this.maxChunks = maxChunks;
        }

        public Boolean getIncludeMetadata() {
            return includeMetadata;
        }

        public void setIncludeMetadata(Boolean includeMetadata) {
            this.includeMetadata = includeMetadata;
        }
    }
}
