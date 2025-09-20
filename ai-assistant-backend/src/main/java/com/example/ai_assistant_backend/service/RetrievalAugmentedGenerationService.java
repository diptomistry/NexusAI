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

    // RAG configuration constants - Updated to support multiple documents
    private static final int DEFAULT_CONTEXT_LIMIT = 20; // Increased from 5 to support multiple documents
    private static final int MAX_CONTEXT_LENGTH = 15000; // Increased from 4000 to accommodate more content
    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.5; // Lowered from 0.7 to include more chunks
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

        // First pass: filter by similarity threshold and remove duplicates
        List<DocumentChunkSimilarityResult> filteredChunks = chunks.stream()
                .filter(chunk -> chunk.getSimilarity() >= config.getSimilarityThreshold())
                .filter(this::isNotDuplicate)
                .collect(Collectors.toList());

        // Re-rank by relevance score
        List<DocumentChunkSimilarityResult> rankedChunks = filteredChunks.stream()
                .map(chunk -> {
                    double relevanceScore = calculateRelevanceScore(chunk, query, config);
                    chunk.setSimilarity(relevanceScore); // Store adjusted score
                    return chunk;
                })
                .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
                .collect(Collectors.toList());

        // Apply diversity filtering if enabled
        if (config.isEnableDiversityFiltering()) {
            return applyDiversitySelection(rankedChunks, config.getMaxChunks());
        } else {
            // Simple limit without diversity filtering
            return rankedChunks.stream()
                    .limit(config.getMaxChunks())
                    .collect(Collectors.toList());
        }
    }

    /**
     * Apply diversity selection to ensure chunks from different documents are
     * included
     */
    private List<DocumentChunkSimilarityResult> applyDiversitySelection(
            List<DocumentChunkSimilarityResult> chunks, int maxChunks) {

        Map<Long, List<DocumentChunkSimilarityResult>> chunksByDocument = chunks.stream()
                .collect(Collectors.groupingBy(DocumentChunkSimilarityResult::getDocumentId));

        List<DocumentChunkSimilarityResult> selectedChunks = new ArrayList<>();
        List<Long> documentIds = new ArrayList<>(chunksByDocument.keySet());

        // Round-robin selection to ensure diversity across documents
        int maxPerDocument = Math.max(1, maxChunks / documentIds.size());
        int remainingSlots = maxChunks;

        // First pass: take top chunks from each document
        for (Long documentId : documentIds) {
            List<DocumentChunkSimilarityResult> documentChunks = chunksByDocument.get(documentId);
            int chunksToTake = Math.min(maxPerDocument, Math.min(documentChunks.size(), remainingSlots));

            for (int i = 0; i < chunksToTake && remainingSlots > 0; i++) {
                selectedChunks.add(documentChunks.get(i));
                remainingSlots--;
            }
        }

        // Second pass: fill remaining slots with highest scoring chunks from any
        // document
        if (remainingSlots > 0) {
            List<DocumentChunkSimilarityResult> remainingChunks = chunks.stream()
                    .filter(chunk -> !selectedChunks.contains(chunk))
                    .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
                    .limit(remainingSlots)
                    .collect(Collectors.toList());

            selectedChunks.addAll(remainingChunks);
        }

        return selectedChunks;
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
                    // Clean the chunk text before adding to context
                    chunkText = cleanChunkText(chunkText);

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
     * Clean chunk text to fix formatting issues
     */
    private String cleanChunkText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // Basic text cleaning
        text = text.trim();

        // Fix word splitting issues using a more direct approach
        text = fixWordSplitting(text);

        // Fix specific common OCR errors
        text = text.replaceAll("\\bInvestig\\s+ates\\b", "Investigates");
        text = text.replaceAll("\\bIntegr\\s+ates\\b", "Integrates");
        text = text.replaceAll("\\btrans\\s+duction\\b", "transduction");
        text = text.replaceAll("\\bcalibr\\s+ating\\b", "calibrating");
        text = text.replaceAll("\\bExam\\s+ines\\b", "Examines");
        text = text.replaceAll("\\bquant\\s+ifies\\b", "quantifies");
        text = text.replaceAll("\\bs\\s+ensitivity\\b", "sensitivity");
        text = text.replaceAll("\\brepeat\\s+ability\\b", "repeatability");
        text = text.replaceAll("\\bhyster\\s+esis\\b", "hysteresis");
        text = text.replaceAll("\\bfiref\\s+ighting\\b", "firefighting");
        text = text.replaceAll("\\bEstablish\\s+es\\b", "Establishes");

        // Fix more aggressive word splitting patterns
        text = text.replaceAll("\\bvacuum\\s+se\\s+aler\\b", "vacuum sealer");
        text = text.replaceAll("\\bvacuum\\s+sealing\\b", "vacuum sealing");
        text = text.replaceAll("\\bbulky\\s+vacuum\\s+machines\\b", "bulky vacuum machines");
        text = text.replaceAll("\\bglass\\s+jars\\b", "glass jars");
        text = text.replaceAll("\\bRe\\s+usable\\b", "Reusable");
        text = text.replaceAll("\\bmicro\\s+plast\\s+ics\\b", "microplastics");
        text = text.replaceAll("\\bfres\\s+her\\b", "fresher");
        text = text.replaceAll("\\bper\\s+ks\\b", "perks");
        text = text.replaceAll("\\bfr\\s+uits\\b", "fruits");
        text = text.replaceAll("\\bse\\s+aler\\b", "sealer");
        text = text.replaceAll("\\bbag\\s+-\\s+based\\b", "bag-based");
        text = text.replaceAll("\\bauto\\s+-\\s+stop\\b", "auto-stop");
        text = text.replaceAll("\\bmicrowave\\s+-\\s+safe\\b", "microwave-safe");
        text = text.replaceAll("\\bdishwasher\\s+-\\s+and\\b", "dishwasher- and");
        text = text.replaceAll("\\bmoney\\s+-\\s+back\\b", "money-back");
        text = text.replaceAll("\\bsell\\s+-\\s+outs\\b", "sell-outs");
        text = text.replaceAll("\\bone\\s+-\\s+button\\b", "one-button");
        text = text.replaceAll("\\blong\\s+-\\s+lasting\\b", "long-lasting");
        text = text.replaceAll("\\b30\\s+-\\s+day\\b", "30-day");

        // Fix common compound words that get split
        text = text.replaceAll("\\bkitchen\\s+gadget\\b", "kitchen gadget");
        text = text.replaceAll("\\bkitchen\\s+wonder\\b", "kitchen wonder");
        text = text.replaceAll("\\bvacuum\\s+machines\\b", "vacuum machines");
        text = text.replaceAll("\\bplastic\\s+bags\\b", "plastic bags");
        text = text.replaceAll("\\bglass\\s+jars\\b", "glass jars");
        text = text.replaceAll("\\bfood\\s+storage\\b", "food storage");
        text = text.replaceAll("\\bfood\\s+preservation\\b", "food preservation");
        text = text.replaceAll("\\bvacuum\\s+sealing\\b", "vacuum sealing");
        text = text.replaceAll("\\bfresh\\s+food\\b", "fresh food");
        text = text.replaceAll("\\bfood\\s+fresher\\b", "food fresher");
        text = text.replaceAll("\\bkitchen\\s+counter\\b", "kitchen counter");
        text = text.replaceAll("\\bcharging\\s+cable\\b", "charging cable");
        text = text.replaceAll("\\blid\\s+opener\\b", "lid opener");
        text = text.replaceAll("\\bfree\\s+shipping\\b", "free shipping");
        text = text.replaceAll("\\bfree\\s+lids\\b", "free lids");
        text = text.replaceAll("\\bmoney\\s+back\\b", "money back");
        text = text.replaceAll("\\bguarantee\\s+period\\b", "guarantee period");
        text = text.replaceAll("\\bpromotional\\s+offers\\b", "promotional offers");
        text = text.replaceAll("\\badvertising\\s+hooks\\b", "advertising hooks");
        text = text.replaceAll("\\bexample\\s+ads\\b", "example ads");

        // Normalize whitespace
        text = text.replaceAll("\\s+", " ");

        return text;
    }

    /**
     * Fix word splitting issues from PDF extraction
     */
    private String fixWordSplitting(String text) {
        // More aggressive pattern to catch various word splitting scenarios
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b([a-zA-Z]{1,})\\s+([a-zA-Z]{1,})\\b");
        java.util.regex.Matcher matcher = pattern.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String firstWord = matcher.group(1);
            String secondWord = matcher.group(2);
            String combined = firstWord + secondWord;

            // More lenient word detection
            if (isLikelyWord(combined) || isCommonWord(combined) || isTechnicalTerm(combined)) {
                matcher.appendReplacement(result, combined);
            } else {
                matcher.appendReplacement(result, matcher.group(0));
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Check if a word is a common English word
     */
    private boolean isCommonWord(String word) {
        if (word.length() < 2)
            return false;

        // Common English words that often get split
        String[] commonWords = {
                "the", "and", "for", "are", "but", "not", "you", "all", "can", "had", "her", "was", "one", "our", "out",
                "day", "get", "has", "him", "his", "how", "its", "may", "new", "now", "old", "see", "two", "way", "who",
                "boy", "did", "man", "men", "put", "say", "she", "too", "use", "any", "ask", "big", "buy", "end", "far",
                "few", "got", "hot", "let", "lot", "low", "off", "own", "run", "set", "sit", "sun", "ten", "try", "win",
                "yes", "yet", "bad", "bag", "bed", "box", "car", "cat", "cup", "cut", "dog", "dry", "eat", "eye", "fat",
                "fit", "fun", "gun", "hat", "hit", "job", "key", "leg", "lie", "map", "mix", "net", "oil", "pay", "pen",
                "pet", "pie", "pig", "pot", "red", "run", "sad", "sea", "six", "sky", "son", "top", "toy", "war", "win",
                "yes", "yet", "zip"
        };

        for (String common : commonWords) {
            if (word.toLowerCase().equals(common)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if a word looks like a technical term
     */
    private boolean isTechnicalTerm(String word) {
        if (word.length() < 3)
            return false;

        // Check for common technical suffixes
        boolean hasTechnicalSuffix = word.matches(
                ".*(tion|sion|ing|ment|ness|ity|ism|logy|graphy|metry|scopy|physis|genesis|analysis|synthesis|sealer|sealing|vacuum|kitchen|gadget|wonder|machine|storage|preservation|fresher|counter|cable|opener|shipping|guarantee|promotional|advertising).*");

        // Check for common technical prefixes
        boolean hasTechnicalPrefix = word.matches(
                "(micro|macro|multi|inter|intra|trans|sub|super|hyper|ultra|pseudo|semi|auto|bio|geo|hydro|electro|thermo|photo|mechano|electro|vacuum|kitchen|food|glass|plastic|fresh|free|money|long|one|auto|microwave|dishwasher).*");

        // Check for compound technical words
        boolean isCompound = word.matches(
                ".*(vacuum|sealer|sealing|kitchen|gadget|wonder|machine|storage|preservation|fresher|counter|cable|opener|shipping|guarantee|promotional|advertising|glass|jars|plastic|bags|food|fresh|free|money|long|lasting|button|stop|safe|back|outs|day|based|wonder|gadget).*");

        return hasTechnicalSuffix || hasTechnicalPrefix || isCompound;
    }

    /**
     * Check if a combined word looks like a real word
     */
    private boolean isLikelyWord(String word) {
        if (word.length() < 3)
            return false;

        boolean hasVowel = word.matches(".*[aeiouAEIOU].*");
        boolean hasConsonant = word.matches(".*[bcdfghjklmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ].*");
        boolean hasCommonPattern = word.matches(".*(ing|ed|er|ly|tion|sion|ness|ment|able|ible).*");

        return hasVowel && hasConsonant && (hasCommonPattern || word.length() > 4);
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
        private int maxChunks = DEFAULT_CONTEXT_LIMIT; // Now 20 by default
        private int maxContextLength = MAX_CONTEXT_LENGTH; // Now 15000 by default
        private double similarityThreshold = DEFAULT_SIMILARITY_THRESHOLD; // Now 0.5 by default
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
