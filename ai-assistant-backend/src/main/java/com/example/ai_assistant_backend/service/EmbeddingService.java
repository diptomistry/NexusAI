package com.example.ai_assistant_backend.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Advanced Embedding Service with OpenAI integration
 * Demonstrates enterprise patterns: retry logic, caching, async processing,
 * batch operations
 */
@Service
public class EmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingService.class);

    private static final String OPENAI_EMBEDDINGS_URL = "https://api.openai.com/v1/embeddings";
    private static final String EMBEDDING_MODEL = "text-embedding-ada-002";
    private static final int MAX_BATCH_SIZE = 100;
    private static final int MAX_TOKENS_PER_CHUNK = 8191; // OpenAI limit

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    private final RestTemplate restTemplate;
    private final Executor embeddingExecutor;

    public EmbeddingService() {
        this.restTemplate = new RestTemplate();
        this.embeddingExecutor = Executors.newFixedThreadPool(5); // Configurable thread pool
    }

    /**
     * Generate embedding for a single text with caching
     */
    @Cacheable(value = "embeddings", key = "#text.hashCode()")
    public float[] generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }

        if (openaiApiKey == null || openaiApiKey.trim().isEmpty()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }

        try {
            Instant start = Instant.now();

            // Truncate text if too long
            String processedText = truncateText(text, MAX_TOKENS_PER_CHUNK);

            EmbeddingRequest request = new EmbeddingRequest(EMBEDDING_MODEL, List.of(processedText));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            HttpEntity<EmbeddingRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<EmbeddingResponse> response = restTemplate.exchange(
                    OPENAI_EMBEDDINGS_URL,
                    HttpMethod.POST,
                    entity,
                    EmbeddingResponse.class);

            if (response.getBody() == null || response.getBody().getData().isEmpty()) {
                throw new RuntimeException("Empty response from OpenAI API");
            }

            List<Double> embeddingList = response.getBody().getData().get(0).getEmbedding();
            float[] embedding = new float[embeddingList.size()];
            for (int i = 0; i < embeddingList.size(); i++) {
                embedding[i] = embeddingList.get(i).floatValue();
            }

            Duration duration = Duration.between(start, Instant.now());
            logger.info("Generated embedding for text length {} in {}ms",
                    processedText.length(), duration.toMillis());

            return embedding;

        } catch (Exception e) {
            logger.error("Failed to generate embedding for text: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate embedding: " + e.getMessage(), e);
        }
    }

    /**
     * Generate embeddings for multiple texts in batches (async)
     */
    public CompletableFuture<Map<String, float[]>> generateEmbeddingsBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyMap());
        }

        // Split into batches and process in parallel
        List<List<String>> batches = createBatches(texts, MAX_BATCH_SIZE);

        List<CompletableFuture<Map<String, float[]>>> futures = batches.stream()
                .map(batch -> CompletableFuture.supplyAsync(
                        () -> generateEmbeddingsForBatch(batch),
                        embeddingExecutor))
                .collect(Collectors.toList());

        // Combine results when all complete
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    Map<String, float[]> results = new HashMap<>();
                    futures.forEach(future -> results.putAll(future.join()));
                    return results;
                });
    }

    /**
     * Process a single batch of texts
     */
    private Map<String, float[]> generateEmbeddingsForBatch(List<String> texts) {
        Map<String, float[]> results = new HashMap<>();

        try {
            // Process texts that need truncation
            List<String> processedTexts = texts.stream()
                    .map(text -> truncateText(text, MAX_TOKENS_PER_CHUNK))
                    .collect(Collectors.toList());

            EmbeddingRequest request = new EmbeddingRequest(EMBEDDING_MODEL, processedTexts);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            HttpEntity<EmbeddingRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<EmbeddingResponse> response = restTemplate.exchange(
                    OPENAI_EMBEDDINGS_URL,
                    HttpMethod.POST,
                    entity,
                    EmbeddingResponse.class);

            if (response.getBody() != null && response.getBody().getData() != null) {
                List<EmbeddingData> embeddingDataList = response.getBody().getData();

                for (int i = 0; i < texts.size() && i < embeddingDataList.size(); i++) {
                    List<Double> embeddingList = embeddingDataList.get(i).getEmbedding();
                    float[] embedding = new float[embeddingList.size()];
                    for (int j = 0; j < embeddingList.size(); j++) {
                        embedding[j] = embeddingList.get(j).floatValue();
                    }
                    results.put(texts.get(i), embedding);
                }
            }

        } catch (Exception e) {
            logger.error("Failed to process batch: {}", e.getMessage(), e);
            throw new RuntimeException("Batch processing failed: " + e.getMessage(), e);
        }

        return results;
    }

    /**
     * Convert float array to PostgreSQL vector format
     */
    public String embeddingToVectorString(float[] embedding) {
        if (embedding == null || embedding.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Convert PostgreSQL vector string back to float array
     */
    public float[] vectorStringToEmbedding(String vectorString) {
        if (vectorString == null || vectorString.trim().isEmpty()) {
            return new float[0];
        }

        // Remove brackets and split by commas
        String clean = vectorString.replaceAll("[\\[\\]\\s]", "");
        if (clean.isEmpty()) {
            return new float[0];
        }

        String[] parts = clean.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i]);
        }
        return result;
    }

    /**
     * Calculate cosine similarity between two embeddings
     */
    public double calculateCosineSimilarity(float[] embedding1, float[] embedding2) {
        if (embedding1 == null || embedding2 == null ||
                embedding1.length != embedding2.length ||
                embedding1.length == 0) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < embedding1.length; i++) {
            double v1 = embedding1[i];
            double v2 = embedding2[i];
            dotProduct += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }

        if (norm1 <= 0 || norm2 <= 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Estimate token count (rough approximation)
     */
    public int estimateTokenCount(String text) {
        if (text == null)
            return 0;
        // Rough estimate: 1 token ≈ 4 words
        String[] words = text.trim().split("\\s+");
        return (int) Math.ceil(words.length / 4.0);
    }

    /**
     * Truncate text to fit within token limits
     */
    private String truncateText(String text, int maxTokens) {
        if (text == null)
            return "";

        int estimatedTokens = estimateTokenCount(text);
        if (estimatedTokens <= maxTokens) {
            return text;
        }

        // Truncate to approximately maxTokens worth of words (4 words per token)
        String[] words = text.trim().split("\\s+");
        int maxWords = maxTokens * 4;
        if (words.length <= maxWords) {
            return text;
        }

        // Join first maxWords words
        StringBuilder truncated = new StringBuilder();
        for (int i = 0; i < maxWords && i < words.length; i++) {
            if (i > 0)
                truncated.append(" ");
            truncated.append(words[i]);
        }
        return truncated.toString();
    }

    /**
     * Create batches from a list
     */
    private <T> List<List<T>> createBatches(List<T> items, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < items.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, items.size());
            batches.add(items.subList(i, endIndex));
        }
        return batches;
    }

    // DTOs for OpenAI API
    private static class EmbeddingRequest {
        private final String model;
        private final List<String> input;

        public EmbeddingRequest(String model, List<String> input) {
            this.model = model;
            this.input = input;
        }

        @SuppressWarnings("unused")
        public String getModel() {
            return model;
        }

        @SuppressWarnings("unused")
        public List<String> getInput() {
            return input;
        }
    }

    private static class EmbeddingResponse {
        @JsonProperty("data")
        private List<EmbeddingData> data;

        @SuppressWarnings("unused")
        public List<EmbeddingData> getData() {
            return data;
        }

        @SuppressWarnings("unused")
        public void setData(List<EmbeddingData> data) {
            this.data = data;
        }
    }

    private static class EmbeddingData {
        @JsonProperty("embedding")
        private List<Double> embedding;

        @SuppressWarnings("unused")
        public List<Double> getEmbedding() {
            return embedding;
        }

        @SuppressWarnings("unused")
        public void setEmbedding(List<Double> embedding) {
            this.embedding = embedding;
        }
    }
}
