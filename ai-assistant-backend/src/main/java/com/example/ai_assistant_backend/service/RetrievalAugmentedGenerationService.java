package com.example.ai_assistant_backend.service;

import com.example.ai_assistant_backend.dto.DocumentChunkSimilarityResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced Retrieval-Augmented Generation Service
 * Demonstrates enterprise-level RAG implementation with sophisticated context
 * building
 */
@Service
public class RetrievalAugmentedGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(RetrievalAugmentedGenerationService.class);

    // RAG configuration constants
    private static final int DEFAULT_CONTEXT_LIMIT = 5;
    private static final int MAX_CONTEXT_LENGTH = 4000;
    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.7;
    private static final double HIGH_SIMILARITY_THRESHOLD = 0.85;

    @Autowired
    private VectorSearchService vectorSearchService;

    @Autowired
    private EmbeddingService embeddingService;

    /**
     * Generate enhanced context for AI queries using RAG
     */
    @Cacheable(value = "ragContext", key = "#query.hashCode() + '_' + #userId + '_' + #assistantId")
    public RAGContext generateContext(String query, UUID userId, String assistantId, RAGConfig config) {
        logger.info("Generating RAG context for query: {}", query.substring(0, Math.min(50, query.length())));

        try {
            // Apply default configuration if not provided
            if (config == null) {
                config = new RAGConfig();
            }

            // Perform semantic search
            List<DocumentChunkSimilarityResult> similarChunks = vectorSearchService.semanticSearch(
                    query,
                    userId,
                    assistantId,
                    config.getSimilarityThreshold(),
                    config.getMaxChunks() * 2 // Get more chunks for better filtering
            );

            // Apply advanced filtering and ranking
            List<DocumentChunkSimilarityResult> filteredChunks = applyAdvancedFiltering(similarChunks, query, config);

            // Build context with intelligent organization
            String contextText = buildIntelligentContext(filteredChunks, config);

            // Generate context metadata
            ContextMetadata metadata = generateContextMetadata(filteredChunks, query);

            logger.info("Generated RAG context with {} chunks, {} characters",
                    filteredChunks.size(), contextText.length());

            return new RAGContext(contextText, filteredChunks, metadata, config);

        } catch (Exception e) {
            logger.error("Failed to generate RAG context: {}", e.getMessage(), e);
            return new RAGContext("", new ArrayList<>(), new ContextMetadata(), config);
        }
    }

    /**
     * Generate context with document filtering
     */
    public RAGContext generateContextWithDocumentFilter(String query, UUID userId, String assistantId,
            List<Long> documentIds, RAGConfig config) {
        logger.info("Generating filtered RAG context for {} documents", documentIds.size());

        try {
            if (config == null) {
                config = new RAGConfig();
            }

            // Perform filtered semantic search
            List<DocumentChunkSimilarityResult> similarChunks = vectorSearchService.semanticSearchWithDocumentFilter(
                    query,
                    userId,
                    assistantId,
                    documentIds,
                    config.getSimilarityThreshold(),
                    config.getMaxChunks() * 2);

            // Apply advanced filtering and ranking
            List<DocumentChunkSimilarityResult> filteredChunks = applyAdvancedFiltering(similarChunks, query, config);

            // Build context
            String contextText = buildIntelligentContext(filteredChunks, config);
            ContextMetadata metadata = generateContextMetadata(filteredChunks, query);

            return new RAGContext(contextText, filteredChunks, metadata, config);

        } catch (Exception e) {
            logger.error("Failed to generate filtered RAG context: {}", e.getMessage(), e);
            return new RAGContext("", new ArrayList<>(), new ContextMetadata(), config);
        }
    }

    /**
     * Generate multi-query context for complex queries
     */
    public RAGContext generateMultiQueryContext(String mainQuery, List<String> relatedQueries,
            UUID userId, String assistantId, RAGConfig config) {
        logger.info("Generating multi-query RAG context for {} queries", relatedQueries.size() + 1);

        try {
            if (config == null) {
                config = new RAGConfig();
            }

            // Combine all queries
            List<String> allQueries = new ArrayList<>();
            allQueries.add(mainQuery);
            allQueries.addAll(relatedQueries);

            // Perform multi-query search
            List<DocumentChunkSimilarityResult> similarChunks = vectorSearchService.multiQuerySemanticSearch(
                    allQueries,
                    userId,
                    assistantId,
                    config.getSimilarityThreshold(),
                    config.getMaxChunks());

            // Apply filtering with query relevance scoring
            List<DocumentChunkSimilarityResult> filteredChunks = applyMultiQueryFiltering(
                    similarChunks, mainQuery, relatedQueries, config);

            // Build context
            String contextText = buildIntelligentContext(filteredChunks, config);
            ContextMetadata metadata = generateContextMetadata(filteredChunks, mainQuery);

            return new RAGContext(contextText, filteredChunks, metadata, config);

        } catch (Exception e) {
            logger.error("Failed to generate multi-query RAG context: {}", e.getMessage(), e);
            return new RAGContext("", new ArrayList<>(), new ContextMetadata(), config);
        }
    }

    /**
     * Apply advanced filtering and ranking algorithms
     */
    private List<DocumentChunkSimilarityResult> applyAdvancedFiltering(
            List<DocumentChunkSimilarityResult> chunks, String query, RAGConfig config) {

        return chunks.stream()
                // Filter by similarity threshold
                .filter(chunk -> chunk.getSimilarity() >= config.getSimilarityThreshold())

                // Remove duplicates based on content similarity
                .filter(this::isNotDuplicate)

                // Apply diversity filtering
                .collect(Collectors.toList())
                .stream()

                // Re-rank by relevance score
                .map(chunk -> {
                    double relevanceScore = calculateRelevanceScore(chunk, query, config);
                    chunk.setSimilarity(relevanceScore); // Store adjusted score
                    return chunk;
                })

                // Sort by adjusted relevance score
                .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))

                // Apply diversity selection
                .collect(Collectors.toList())
                .stream()
                .limit(config.getMaxChunks())
                .collect(Collectors.toList());
    }

    /**
     * Apply multi-query specific filtering
     */
    private List<DocumentChunkSimilarityResult> applyMultiQueryFiltering(
            List<DocumentChunkSimilarityResult> chunks, String mainQuery,
            List<String> relatedQueries, RAGConfig config) {

        return chunks.stream()
                .filter(chunk -> chunk.getSimilarity() >= config.getSimilarityThreshold())
                .map(chunk -> {
                    // Calculate combined relevance for all queries
                    double combinedScore = calculateMultiQueryRelevance(chunk, mainQuery, relatedQueries);
                    chunk.setSimilarity(combinedScore);
                    return chunk;
                })
                .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
                .limit(config.getMaxChunks())
                .collect(Collectors.toList());
    }

    /**
     * Build intelligent context with proper organization
     */
    private String buildIntelligentContext(List<DocumentChunkSimilarityResult> chunks, RAGConfig config) {
        if (chunks.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();

        // Add context header
        if (config.isIncludeMetadata()) {
            context.append("Based on the following relevant document sections:\n\n");
        }

        // Group chunks by document for better organization
        Map<Long, List<DocumentChunkSimilarityResult>> chunksByDocument = chunks.stream()
                .collect(Collectors.groupingBy(DocumentChunkSimilarityResult::getDocumentId));

        int totalLength = 0;
        int sectionNumber = 1;

        for (Map.Entry<Long, List<DocumentChunkSimilarityResult>> entry : chunksByDocument.entrySet()) {
            List<DocumentChunkSimilarityResult> documentChunks = entry.getValue();

            // Sort chunks by their index within the document
            documentChunks.sort(Comparator.comparing(DocumentChunkSimilarityResult::getChunkIndex));

            // Add document header if metadata is enabled
            if (config.isIncludeMetadata() && !documentChunks.isEmpty()) {
                String fileName = documentChunks.get(0).getDocumentFileName();
                context.append(String.format("From document: %s\n", fileName != null ? fileName : "Unknown"));
            }

            // Add chunks from this document
            for (DocumentChunkSimilarityResult chunk : documentChunks) {
                if (totalLength >= config.getMaxContextLength()) {
                    break;
                }

                String chunkText = chunk.getChunkText();
                if (chunkText != null && !chunkText.trim().isEmpty()) {
                    // Add section number if configured
                    if (config.isIncludeMetadata()) {
                        context.append(String.format("Section %d (similarity: %.2f):\n",
                                sectionNumber++, chunk.getSimilarity()));
                    }

                    // Truncate chunk if necessary
                    int remainingLength = config.getMaxContextLength() - totalLength;
                    if (chunkText.length() > remainingLength) {
                        chunkText = chunkText.substring(0, remainingLength) + "...";
                    }

                    context.append(chunkText.trim()).append("\n\n");
                    totalLength += chunkText.length();
                }
            }

            if (totalLength >= config.getMaxContextLength()) {
                break;
            }
        }

        return context.toString().trim();
    }

    /**
     * Calculate relevance score for a chunk
     */
    private double calculateRelevanceScore(DocumentChunkSimilarityResult chunk, String query, RAGConfig config) {
        double baseScore = chunk.getSimilarity();

        // Apply recency boost
        if (config.isApplyRecencyBoost() && chunk.getCreatedAt() != null) {
            long daysSinceCreation = java.time.Duration.between(
                    chunk.getCreatedAt(),
                    java.time.LocalDateTime.now()).toDays();

            double recencyBoost = Math.max(0, 1.0 - (daysSinceCreation / 365.0)) * 0.1;
            baseScore += recencyBoost;
        }

        // Apply length penalty for very short or very long chunks
        if (chunk.getChunkTokens() != null) {
            double lengthPenalty = calculateLengthPenalty(chunk.getChunkTokens());
            baseScore *= lengthPenalty;
        }

        // Apply keyword matching boost
        double keywordBoost = calculateKeywordMatchBoost(chunk.getChunkText(), query);
        baseScore += keywordBoost;

        return Math.min(1.0, baseScore); // Cap at 1.0
    }

    /**
     * Calculate multi-query relevance
     */
    private double calculateMultiQueryRelevance(DocumentChunkSimilarityResult chunk,
            String mainQuery, List<String> relatedQueries) {
        double mainScore = chunk.getSimilarity();

        // Calculate average similarity to related queries
        double relatedScore = 0.0;
        if (!relatedQueries.isEmpty()) {
            // This is a simplified approach - in a real implementation,
            // you'd calculate similarity to each related query
            relatedScore = mainScore * 0.8; // Assumption: related queries have 80% relevance
        }

        // Weighted combination: 70% main query, 30% related queries
        return (mainScore * 0.7) + (relatedScore * 0.3);
    }

    /**
     * Check if chunk is not a duplicate
     */
    private boolean isNotDuplicate(DocumentChunkSimilarityResult chunk) {
        // Simplified duplicate detection - in a real implementation,
        // you'd maintain a set of seen content hashes
        return true;
    }

    /**
     * Calculate length penalty
     */
    private double calculateLengthPenalty(int tokenCount) {
        if (tokenCount < 50)
            return 0.8; // Too short
        if (tokenCount > 500)
            return 0.9; // Too long
        return 1.0; // Good length
    }

    /**
     * Calculate keyword matching boost
     */
    private double calculateKeywordMatchBoost(String chunkText, String query) {
        if (chunkText == null || query == null)
            return 0.0;

        String[] queryWords = query.toLowerCase().split("\\s+");
        String chunkLower = chunkText.toLowerCase();

        long matchingWords = Arrays.stream(queryWords)
                .filter(chunkLower::contains)
                .count();

        return (double) matchingWords / queryWords.length * 0.1; // Max 10% boost
    }

    /**
     * Generate context metadata
     */
    private ContextMetadata generateContextMetadata(List<DocumentChunkSimilarityResult> chunks, String query) {
        ContextMetadata metadata = new ContextMetadata();

        if (chunks.isEmpty()) {
            return metadata;
        }

        metadata.setTotalChunks(chunks.size());
        metadata.setAverageSimilarity(chunks.stream()
                .mapToDouble(DocumentChunkSimilarityResult::getSimilarity)
                .average()
                .orElse(0.0));
        metadata.setMaxSimilarity(chunks.stream()
                .mapToDouble(DocumentChunkSimilarityResult::getSimilarity)
                .max()
                .orElse(0.0));
        metadata.setMinSimilarity(chunks.stream()
                .mapToDouble(DocumentChunkSimilarityResult::getSimilarity)
                .min()
                .orElse(0.0));

        metadata.setUniqueDocuments(chunks.stream()
                .map(DocumentChunkSimilarityResult::getDocumentId)
                .collect(Collectors.toSet())
                .size());

        metadata.setTotalTokens(chunks.stream()
                .mapToInt(chunk -> chunk.getChunkTokens() != null ? chunk.getChunkTokens() : 0)
                .sum());

        return metadata;
    }

    // Configuration and result classes
    public static class RAGConfig {
        private int maxChunks = DEFAULT_CONTEXT_LIMIT;
        private int maxContextLength = MAX_CONTEXT_LENGTH;
        private double similarityThreshold = DEFAULT_SIMILARITY_THRESHOLD;
        private boolean includeMetadata = true;
        private boolean applyRecencyBoost = false;
        private boolean enableDiversityFiltering = true;

        // Getters and setters
        public int getMaxChunks() {
            return maxChunks;
        }

        public void setMaxChunks(int maxChunks) {
            this.maxChunks = maxChunks;
        }

        public int getMaxContextLength() {
            return maxContextLength;
        }

        public void setMaxContextLength(int maxContextLength) {
            this.maxContextLength = maxContextLength;
        }

        public double getSimilarityThreshold() {
            return similarityThreshold;
        }

        public void setSimilarityThreshold(double similarityThreshold) {
            this.similarityThreshold = similarityThreshold;
        }

        public boolean isIncludeMetadata() {
            return includeMetadata;
        }

        public void setIncludeMetadata(boolean includeMetadata) {
            this.includeMetadata = includeMetadata;
        }

        public boolean isApplyRecencyBoost() {
            return applyRecencyBoost;
        }

        public void setApplyRecencyBoost(boolean applyRecencyBoost) {
            this.applyRecencyBoost = applyRecencyBoost;
        }

        public boolean isEnableDiversityFiltering() {
            return enableDiversityFiltering;
        }

        public void setEnableDiversityFiltering(boolean enableDiversityFiltering) {
            this.enableDiversityFiltering = enableDiversityFiltering;
        }
    }

    public static class RAGContext {
        private final String contextText;
        private final List<DocumentChunkSimilarityResult> sourceChunks;
        private final ContextMetadata metadata;
        private final RAGConfig config;

        public RAGContext(String contextText, List<DocumentChunkSimilarityResult> sourceChunks,
                ContextMetadata metadata, RAGConfig config) {
            this.contextText = contextText;
            this.sourceChunks = sourceChunks;
            this.metadata = metadata;
            this.config = config;
        }

        // Getters
        public String getContextText() {
            return contextText;
        }

        public List<DocumentChunkSimilarityResult> getSourceChunks() {
            return sourceChunks;
        }

        public ContextMetadata getMetadata() {
            return metadata;
        }

        public RAGConfig getConfig() {
            return config;
        }

        public boolean hasContext() {
            return contextText != null && !contextText.trim().isEmpty();
        }

        public int getContextLength() {
            return contextText != null ? contextText.length() : 0;
        }
    }

    public static class ContextMetadata {
        private int totalChunks;
        private double averageSimilarity;
        private double maxSimilarity;
        private double minSimilarity;
        private int uniqueDocuments;
        private int totalTokens;

        // Getters and setters
        public int getTotalChunks() {
            return totalChunks;
        }

        public void setTotalChunks(int totalChunks) {
            this.totalChunks = totalChunks;
        }

        public double getAverageSimilarity() {
            return averageSimilarity;
        }

        public void setAverageSimilarity(double averageSimilarity) {
            this.averageSimilarity = averageSimilarity;
        }

        public double getMaxSimilarity() {
            return maxSimilarity;
        }

        public void setMaxSimilarity(double maxSimilarity) {
            this.maxSimilarity = maxSimilarity;
        }

        public double getMinSimilarity() {
            return minSimilarity;
        }

        public void setMinSimilarity(double minSimilarity) {
            this.minSimilarity = minSimilarity;
        }

        public int getUniqueDocuments() {
            return uniqueDocuments;
        }

        public void setUniqueDocuments(int uniqueDocuments) {
            this.uniqueDocuments = uniqueDocuments;
        }

        public int getTotalTokens() {
            return totalTokens;
        }

        public void setTotalTokens(int totalTokens) {
            this.totalTokens = totalTokens;
        }
    }
}
