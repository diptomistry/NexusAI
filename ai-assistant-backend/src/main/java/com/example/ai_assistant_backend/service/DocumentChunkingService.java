package com.example.ai_assistant_backend.service;

import com.example.ai_assistant_backend.exception.PreparedStatementConflictException;
import com.example.ai_assistant_backend.model.Document;
import com.example.ai_assistant_backend.model.DocumentChunk;
import com.example.ai_assistant_backend.repository.DocumentChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Advanced Document Chunking Service
 * Demonstrates sophisticated text processing and chunking algorithms
 */
@Service
@Transactional
public class DocumentChunkingService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentChunkingService.class);

    // Chunking configuration
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    private static final int DEFAULT_OVERLAP_SIZE = 200;
    private static final int MIN_CHUNK_SIZE = 100;
    private static final int MAX_CHUNK_SIZE = 2000;

    // Text patterns for intelligent splitting
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[.!?]+\\s+");
    private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("\\n\\s*\\n");
    private static final Pattern SECTION_PATTERN = Pattern.compile("(?i)(chapter|section|part)\\s+\\d+|#{1,6}\\s+");

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Autowired
    private EmbeddingService embeddingService;

    /**
     * Process document and create chunks with embeddings
     */
    @Transactional
    public CompletableFuture<List<DocumentChunk>> processDocumentAsync(Document document) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Starting document chunking for document ID: {}", document.getId());

                // Delete existing chunks for this document
                deleteExistingChunks(document.getId());

                // Create chunks
                List<DocumentChunk> chunks = createIntelligentChunks(document);

                // Save chunks to database in smaller batches with retry logic
                List<DocumentChunk> savedChunks = new ArrayList<>();
                int batchSize = 5; // Very small batch size to avoid prepared statement conflicts
                for (int i = 0; i < chunks.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, chunks.size());
                    List<DocumentChunk> batch = chunks.subList(i, end);

                    // Retry logic for prepared statement conflicts
                    boolean success = false;
                    int retryCount = 0;
                    int maxRetries = 3;

                    while (!success && retryCount < maxRetries) {
                        try {
                            savedChunks.addAll(saveChunksWithRetry(batch));
                            success = true;
                        } catch (Exception e) {
                            retryCount++;
                            if (isPreparedStatementConflict(e)) {
                                logger.warn("Prepared statement conflict for batch {}-{}, retry {}/{}", i, end,
                                        retryCount, maxRetries);
                                if (retryCount < maxRetries) {
                                    try {
                                        Thread.sleep(100 * retryCount); // Exponential backoff
                                    } catch (InterruptedException ie) {
                                        Thread.currentThread().interrupt();
                                        throw new RuntimeException("Interrupted during retry", ie);
                                    }
                                } else {
                                    throw new PreparedStatementConflictException(
                                            "Failed to save batch after " + maxRetries + " retries", e);
                                }
                            } else {
                                throw e;
                            }
                        }
                    }
                }

                // Generate embeddings for the saved chunks
                generateEmbeddingsForChunks(new ArrayList<>(savedChunks));

                logger.info("Successfully created {} chunks for document {}", savedChunks.size(), document.getId());

                return savedChunks;

            } catch (Exception e) {
                logger.error("Failed to process document {}: {}", document.getId(), e.getMessage(), e);
                throw new RuntimeException("Document processing failed: " + e.getMessage(), e);
            }
        });
    }

    public void deleteExistingChunks(Long documentId) {
        try {
            documentChunkRepository.deleteByDocumentId(documentId);
        } catch (Exception e) {
            logger.error("Failed to delete existing chunks for document {}: {}", documentId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete existing chunks: " + e.getMessage(), e);
        }
    }

    /**
     * Save chunks with individual retry logic to handle prepared statement
     * conflicts
     */
    private List<DocumentChunk> saveChunksWithRetry(List<DocumentChunk> chunks) {
        List<DocumentChunk> savedChunks = new ArrayList<>();

        for (DocumentChunk chunk : chunks) {
            boolean success = false;
            int retryCount = 0;
            int maxRetries = 3;

            while (!success && retryCount < maxRetries) {
                try {
                    DocumentChunk savedChunk = documentChunkRepository.save(chunk);
                    savedChunks.add(savedChunk);
                    success = true;
                } catch (Exception e) {
                    retryCount++;
                    if (isPreparedStatementConflict(e)) {
                        logger.warn("Prepared statement conflict for chunk {}, retry {}/{}", chunk.getId(), retryCount,
                                maxRetries);
                        if (retryCount < maxRetries) {
                            try {
                                Thread.sleep(50 * retryCount); // Short delay for individual chunks
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException("Interrupted during chunk retry", ie);
                            }
                        } else {
                            throw new PreparedStatementConflictException(
                                    "Failed to save chunk after " + maxRetries + " retries", e);
                        }
                    } else {
                        throw e;
                    }
                }
            }
        }

        return savedChunks;
    }

    /**
     * Check if the exception is related to prepared statement conflicts
     */
    private boolean isPreparedStatementConflict(Exception e) {
        String message = e.getMessage();
        if (message == null)
            return false;

        return message.contains("prepared statement") ||
                message.contains("S_") ||
                (e instanceof JpaSystemException &&
                        e.getCause() != null &&
                        e.getCause().getMessage() != null &&
                        e.getCause().getMessage().contains("prepared statement"));
    }

    /**
     * Save a single chunk with retry logic to handle prepared statement conflicts
     */
    private void saveChunkWithRetry(DocumentChunk chunk) {
        boolean success = false;
        int retryCount = 0;
        int maxRetries = 3;

        while (!success && retryCount < maxRetries) {
            try {
                documentChunkRepository.save(chunk);
                success = true;
            } catch (Exception e) {
                retryCount++;
                if (isPreparedStatementConflict(e)) {
                    logger.warn("Prepared statement conflict for chunk {}, retry {}/{}", chunk.getId(), retryCount,
                            maxRetries);
                    if (retryCount < maxRetries) {
                        try {
                            Thread.sleep(50 * retryCount); // Short delay
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Interrupted during chunk save retry", ie);
                        }
                    } else {
                        throw new PreparedStatementConflictException(
                                "Failed to save chunk after " + maxRetries + " retries", e);
                    }
                } else {
                    throw e;
                }
            }
        }
    }

    /**
     * Create intelligent chunks with context preservation
     */
    public List<DocumentChunk> createIntelligentChunks(Document document) {
        String text = document.getExtractedText();
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // Choose chunking strategy based on document type and content
        ChunkingStrategy strategy = determineChunkingStrategy(document, text);

        return switch (strategy) {
            case SEMANTIC -> createSemanticChunks(document, text);
            case HIERARCHICAL -> createHierarchicalChunks(document, text);
            case FIXED_SIZE -> createFixedSizeChunks(document, text);
            case SENTENCE_BASED -> createSentenceBasedChunks(document, text);
        };
    }

    /**
     * Determine the best chunking strategy for the document
     */
    private ChunkingStrategy determineChunkingStrategy(Document document, String text) {
        String fileType = document.getFileType().toLowerCase();

        // Check for structured content
        if (hasStructuredContent(text)) {
            return ChunkingStrategy.HIERARCHICAL;
        }

        // Check for markdown or HTML
        if (fileType.contains("markdown") || fileType.contains("html") || text.contains("#")) {
            return ChunkingStrategy.HIERARCHICAL;
        }

        // Check for academic/formal documents
        if (hasAcademicStructure(text)) {
            return ChunkingStrategy.SEMANTIC;
        }

        // Check document length
        if (text.length() > 10000) {
            return ChunkingStrategy.SENTENCE_BASED;
        }

        // Default to fixed size for simple documents
        return ChunkingStrategy.FIXED_SIZE;
    }

    /**
     * Create semantic chunks based on topic boundaries
     */
    private List<DocumentChunk> createSemanticChunks(Document document, String text) {
        List<DocumentChunk> chunks = new ArrayList<>();

        // First, split by clear semantic boundaries
        List<String> sections = splitBySemanticBoundaries(text);

        int chunkIndex = 0;
        for (String section : sections) {
            if (section.trim().isEmpty())
                continue;

            // If section is too large, further split it
            if (section.length() > MAX_CHUNK_SIZE) {
                List<String> subChunks = splitLargeSection(section);
                for (String subChunk : subChunks) {
                    DocumentChunk chunk = createChunk(document, subChunk, chunkIndex++);
                    chunks.add(chunk);
                }
            } else if (section.length() >= MIN_CHUNK_SIZE) {
                DocumentChunk chunk = createChunk(document, section, chunkIndex++);
                chunks.add(chunk);
            }
        }

        return chunks;
    }

    /**
     * Create hierarchical chunks preserving document structure
     */
    private List<DocumentChunk> createHierarchicalChunks(Document document, String text) {
        List<DocumentChunk> chunks = new ArrayList<>();

        // Split by sections/chapters first
        List<TextSection> sections = extractHierarchicalSections(text);

        int chunkIndex = 0;
        for (TextSection section : sections) {
            String sectionText = section.getContent();

            if (sectionText.length() <= MAX_CHUNK_SIZE) {
                // Section fits in one chunk
                DocumentChunk chunk = createChunk(document, sectionText, chunkIndex++);
                chunks.add(chunk);
            } else {
                // Split section into smaller chunks with overlap
                List<String> sectionChunks = createOverlappingChunks(sectionText, DEFAULT_CHUNK_SIZE,
                        DEFAULT_OVERLAP_SIZE);
                for (String chunkText : sectionChunks) {
                    DocumentChunk chunk = createChunk(document, chunkText, chunkIndex++);
                    chunks.add(chunk);
                }
            }
        }

        return chunks;
    }

    /**
     * Create fixed-size chunks with intelligent overlap
     */
    private List<DocumentChunk> createFixedSizeChunks(Document document, String text) {
        List<String> chunkTexts = createOverlappingChunks(text, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP_SIZE);
        List<DocumentChunk> chunks = new ArrayList<>();

        for (int i = 0; i < chunkTexts.size(); i++) {
            DocumentChunk chunk = createChunk(document, chunkTexts.get(i), i);
            chunks.add(chunk);
        }

        return chunks;
    }

    /**
     * Create sentence-based chunks
     */
    private List<DocumentChunk> createSentenceBasedChunks(Document document, String text) {
        List<DocumentChunk> chunks = new ArrayList<>();

        // Split into sentences
        String[] sentences = SENTENCE_PATTERN.split(text);

        StringBuilder currentChunk = new StringBuilder();
        int chunkIndex = 0;

        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.isEmpty())
                continue;

            // Check if adding this sentence would exceed chunk size
            if (currentChunk.length() + sentence.length() > DEFAULT_CHUNK_SIZE && currentChunk.length() > 0) {
                // Save current chunk
                String chunkText = currentChunk.toString().trim();
                if (chunkText.length() >= MIN_CHUNK_SIZE) {
                    DocumentChunk chunk = createChunk(document, chunkText, chunkIndex++);
                    chunks.add(chunk);
                }

                // Start new chunk with overlap
                currentChunk = new StringBuilder();
                String[] words = chunkText.split("\\s+");
                if (words.length > 20) {
                    // Add last 20 words as overlap
                    String overlap = String.join(" ", Arrays.copyOfRange(words, words.length - 20, words.length));
                    currentChunk.append(overlap).append(" ");
                }
            }

            currentChunk.append(sentence).append(". ");
        }

        // Add final chunk
        if (currentChunk.length() >= MIN_CHUNK_SIZE) {
            DocumentChunk chunk = createChunk(document, currentChunk.toString().trim(), chunkIndex);
            chunks.add(chunk);
        }

        return chunks;
    }

    /**
     * Create a document chunk with token count calculation
     */
    private DocumentChunk createChunk(Document document, String text, int index) {
        int tokenCount = embeddingService.estimateTokenCount(text);

        return new DocumentChunk(
                document,
                text.trim(),
                index,
                tokenCount,
                null, // Embedding will be generated later
                UUID.fromString(document.getUserId()),
                document.getAssistantId());
    }

    private List<String> createOverlappingChunks(String text, int chunkSize, int overlapSize) {
        List<String> chunks = new ArrayList<>();

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());

            // Try to find a good boundary (sentence or paragraph end)
            if (end < text.length()) {
                int bestBoundary = findBestBoundary(text, start + chunkSize - 100, start + chunkSize + 100);
                if (bestBoundary > start) {
                    end = bestBoundary;
                }
            }

            String chunk = text.substring(start, end).trim();
            if (chunk.length() >= MIN_CHUNK_SIZE) {
                chunks.add(chunk);
            }

            // Calculate next start position with overlap
            start = Math.max(start + chunkSize - overlapSize, end);
        }

        return chunks;
    }

    /**
     * Find the best boundary for chunk splitting
     */
    /**
     * Generate embeddings for document chunks in batches
     */
    @Transactional
    public void generateEmbeddingsForChunks(List<DocumentChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return;
        }

        logger.info("Starting to generate embeddings for {} chunks", chunks.size());

        try {
            // Process chunks in batches to avoid memory issues and statement conflicts
            int embeddingBatchSize = 5; // Smaller batch size to avoid prepared statement conflicts
            int totalChunks = chunks.size();

            for (int i = 0; i < totalChunks; i += embeddingBatchSize) {
                int end = Math.min(i + embeddingBatchSize, totalChunks);
                List<DocumentChunk> batch = chunks.subList(i, end);

                try {
                    // Generate embeddings for the batch
                    List<String> textsToEmbed = batch.stream()
                            .map(DocumentChunk::getChunkText)
                            .filter(text -> text != null && !text.trim().isEmpty())
                            .collect(Collectors.toList());

                    if (!textsToEmbed.isEmpty()) {
                        // Get the embeddings as a map of text to embedding
                        Map<String, float[]> textToEmbeddingMap = embeddingService.generateEmbeddingsBatch(textsToEmbed)
                                .get();

                        // Update chunks with their corresponding embeddings
                        for (DocumentChunk chunk : batch) {
                            if (chunk.getChunkText() != null && !chunk.getChunkText().trim().isEmpty()) {
                                float[] embedding = textToEmbeddingMap.get(chunk.getChunkText().trim());
                                if (embedding != null) {
                                    chunk.setEmbedding(embedding);
                                }
                            }
                        }

                        // Save the updated chunks individually to avoid statement conflicts
                        for (DocumentChunk chunk : batch) {
                            saveChunkWithRetry(chunk);
                        }

                        logger.debug("Processed embeddings for batch {}-{}/{}", i, end, totalChunks);
                    }

                } catch (Exception e) {
                    logger.error("Error generating embeddings for batch {}-{}: {}", i, end, e.getMessage(), e);
                    // Continue with next batch even if one fails
                }
            }

            logger.info("Completed generating embeddings for {} chunks", chunks.size());

        } catch (Exception e) {
            logger.error("Failed to generate embeddings for chunks: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate embeddings: " + e.getMessage(), e);
        }
    }

    /**
     * Find the best boundary for chunk splitting
     */
    private int findBestBoundary(String text, int minPos, int maxPos) {
        // Look for sentence endings first
        for (int i = maxPos - 1; i >= minPos; i--) {
            char c = text.charAt(i);
            if (c == '.' || c == '!' || c == '?') {
                if (i + 1 < text.length() && Character.isWhitespace(text.charAt(i + 1))) {
                    return i + 1;
                }
            }
        }

        // Look for paragraph breaks
        for (int i = maxPos - 1; i >= minPos; i--) {
            if (text.charAt(i) == '\n') {
                return i + 1;
            }
        }

        // Look for any whitespace
        for (int i = maxPos - 1; i >= minPos; i--) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i + 1;
            }
        }

        return maxPos; // No good boundary found
    }

    // Helper methods for content analysis
    private boolean hasStructuredContent(String text) {
        return SECTION_PATTERN.matcher(text).find() ||
                text.contains("Table of Contents") ||
                text.contains("Chapter") ||
                text.contains("Section");
    }

    private boolean hasAcademicStructure(String text) {
        return text.contains("Abstract") ||
                text.contains("Introduction") ||
                text.contains("Methodology") ||
                text.contains("References") ||
                text.contains("Bibliography");
    }

    private List<String> splitBySemanticBoundaries(String text) {
        // Simple implementation - split by double newlines and section headers
        List<String> sections = new ArrayList<>();

        String[] paragraphs = PARAGRAPH_PATTERN.split(text);
        StringBuilder currentSection = new StringBuilder();

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty())
                continue;

            // Check if this looks like a section header
            if (SECTION_PATTERN.matcher(paragraph).find() && currentSection.length() > 0) {
                sections.add(currentSection.toString().trim());
                currentSection = new StringBuilder();
            }

            currentSection.append(paragraph).append("\n\n");
        }

        if (currentSection.length() > 0) {
            sections.add(currentSection.toString().trim());
        }

        return sections;
    }

    private List<String> splitLargeSection(String section) {
        return createOverlappingChunks(section, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP_SIZE);
    }

    private List<TextSection> extractHierarchicalSections(String text) {
        List<TextSection> sections = new ArrayList<>();

        String[] lines = text.split("\n");
        StringBuilder currentSection = new StringBuilder();
        String currentTitle = null;
        int level = 0;

        for (String line : lines) {
            line = line.trim();

            // Check if line is a header
            if (line.startsWith("#") || SECTION_PATTERN.matcher(line).find()) {
                // Save previous section
                if (currentSection.length() > 0) {
                    sections.add(new TextSection(currentTitle, currentSection.toString().trim(), level));
                    currentSection = new StringBuilder();
                }

                currentTitle = line;
                level = line.startsWith("#") ? line.indexOf(' ') : 1;
            } else {
                currentSection.append(line).append("\n");
            }
        }

        // Add final section
        if (currentSection.length() > 0) {
            sections.add(new TextSection(currentTitle, currentSection.toString().trim(), level));
        }

        return sections;
    }

    // Enums and inner classes
    private enum ChunkingStrategy {
        SEMANTIC, HIERARCHICAL, FIXED_SIZE, SENTENCE_BASED
    }

    private static class TextSection {
        private final String title;
        private final String content;
        private final int level;

        public TextSection(String title, String content, int level) {
            this.title = title;
            this.content = content;
            this.level = level;
        }

        @SuppressWarnings("unused")
        public String getTitle() {
            return title;
        }

        @SuppressWarnings("unused")
        public String getContent() {
            return content;
        }

        @SuppressWarnings("unused")
        public int getLevel() {
            return level;
        }
    }
}
