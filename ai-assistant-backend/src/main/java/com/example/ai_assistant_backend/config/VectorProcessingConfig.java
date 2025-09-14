package com.example.ai_assistant_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Advanced Vector Processing Configuration
 * Demonstrates enterprise-level configuration management for RAG system
 */
@Configuration
@EnableCaching
@EnableAsync
public class VectorProcessingConfig {

    /**
     * Configure cache manager for embeddings and search results
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(java.util.Arrays.asList("embeddings", "similaritySearch", "ragContext"));
        return cacheManager;
    }

    /**
     * Configure thread pool for async embedding processing
     */
    @Bean(name = "embeddingTaskExecutor")
    public Executor embeddingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("EmbeddingProcessor-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * Configure thread pool for vector search operations
     */
    @Bean(name = "vectorSearchExecutor")
    public Executor vectorSearchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("VectorSearch-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * Vector properties configuration bean
     */
    @Bean
    @ConfigurationProperties(prefix = "vector")
    public VectorProperties vectorProperties() {
        return new VectorProperties();
    }

    /**
     * OpenAI properties configuration bean
     */
    @Bean
    @ConfigurationProperties(prefix = "openai")
    public OpenAIProperties openAIProperties() {
        return new OpenAIProperties();
    }

    /**
     * Application vector properties configuration bean
     */
    @Bean
    @ConfigurationProperties(prefix = "app.vector")
    public ApplicationVectorProperties applicationVectorProperties() {
        return new ApplicationVectorProperties();
    }

    /**
     * Vector processing configuration properties
     */
    public static class VectorProperties {
        private Embedding embedding = new Embedding();
        private Chunking chunking = new Chunking();
        private Search search = new Search();

        public static class Embedding {
            private int dimension = 1536;
            private int batchSize = 100;
            private boolean enableCaching = true;

            // Getters and setters
            public int getDimension() {
                return dimension;
            }

            public void setDimension(int dimension) {
                this.dimension = dimension;
            }

            public int getBatchSize() {
                return batchSize;
            }

            public void setBatchSize(int batchSize) {
                this.batchSize = batchSize;
            }

            public boolean isEnableCaching() {
                return enableCaching;
            }

            public void setEnableCaching(boolean enableCaching) {
                this.enableCaching = enableCaching;
            }
        }

        public static class Chunking {
            private int defaultSize = 1000;
            private int overlapSize = 200;
            private int minSize = 100;
            private int maxSize = 2000;
            private String strategy = "intelligent";

            // Getters and setters
            public int getDefaultSize() {
                return defaultSize;
            }

            public void setDefaultSize(int defaultSize) {
                this.defaultSize = defaultSize;
            }

            public int getOverlapSize() {
                return overlapSize;
            }

            public void setOverlapSize(int overlapSize) {
                this.overlapSize = overlapSize;
            }

            public int getMinSize() {
                return minSize;
            }

            public void setMinSize(int minSize) {
                this.minSize = minSize;
            }

            public int getMaxSize() {
                return maxSize;
            }

            public void setMaxSize(int maxSize) {
                this.maxSize = maxSize;
            }

            public String getStrategy() {
                return strategy;
            }

            public void setStrategy(String strategy) {
                this.strategy = strategy;
            }
        }

        public static class Search {
            private double similarityThreshold = 0.7;
            private int defaultLimit = 10;
            private int maxContextLength = 4000;
            private boolean enableDiversityFiltering = true;

            // Getters and setters
            public double getSimilarityThreshold() {
                return similarityThreshold;
            }

            public void setSimilarityThreshold(double similarityThreshold) {
                this.similarityThreshold = similarityThreshold;
            }

            public int getDefaultLimit() {
                return defaultLimit;
            }

            public void setDefaultLimit(int defaultLimit) {
                this.defaultLimit = defaultLimit;
            }

            public int getMaxContextLength() {
                return maxContextLength;
            }

            public void setMaxContextLength(int maxContextLength) {
                this.maxContextLength = maxContextLength;
            }

            public boolean isEnableDiversityFiltering() {
                return enableDiversityFiltering;
            }

            public void setEnableDiversityFiltering(boolean enableDiversityFiltering) {
                this.enableDiversityFiltering = enableDiversityFiltering;
            }
        }

        // Main getters and setters
        public Embedding getEmbedding() {
            return embedding;
        }

        public void setEmbedding(Embedding embedding) {
            this.embedding = embedding;
        }

        public Chunking getChunking() {
            return chunking;
        }

        public void setChunking(Chunking chunking) {
            this.chunking = chunking;
        }

        public Search getSearch() {
            return search;
        }

        public void setSearch(Search search) {
            this.search = search;
        }
    }

    /**
     * OpenAI API configuration properties
     */
    public static class OpenAIProperties {
        private String apiKey;
        private String embeddingModel = "text-embedding-ada-002";
        private int timeout = 30000;
        private int maxRetries = 3;
        private String baseUrl = "https://api.openai.com/v1";

        // Getters and setters
        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getEmbeddingModel() {
            return embeddingModel;
        }

        public void setEmbeddingModel(String embeddingModel) {
            this.embeddingModel = embeddingModel;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    /**
     * Application-specific configuration
     */
    public static class ApplicationVectorProperties {
        private Processing processing = new Processing();
        private Performance performance = new Performance();

        public static class Processing {
            private int threadPoolSize = 5;
            private boolean batchProcessing = true;
            private boolean enableAsyncProcessing = true;
            private int maxConcurrentJobs = 10;

            // Getters and setters
            public int getThreadPoolSize() {
                return threadPoolSize;
            }

            public void setThreadPoolSize(int threadPoolSize) {
                this.threadPoolSize = threadPoolSize;
            }

            public boolean isBatchProcessing() {
                return batchProcessing;
            }

            public void setBatchProcessing(boolean batchProcessing) {
                this.batchProcessing = batchProcessing;
            }

            public boolean isEnableAsyncProcessing() {
                return enableAsyncProcessing;
            }

            public void setEnableAsyncProcessing(boolean enableAsyncProcessing) {
                this.enableAsyncProcessing = enableAsyncProcessing;
            }

            public int getMaxConcurrentJobs() {
                return maxConcurrentJobs;
            }

            public void setMaxConcurrentJobs(int maxConcurrentJobs) {
                this.maxConcurrentJobs = maxConcurrentJobs;
            }
        }

        public static class Performance {
            private boolean enableMetrics = true;
            private boolean enableDetailedLogging = false;
            private int cacheExpiration = 3600; // seconds
            private int maxCacheSize = 1000;

            // Getters and setters
            public boolean isEnableMetrics() {
                return enableMetrics;
            }

            public void setEnableMetrics(boolean enableMetrics) {
                this.enableMetrics = enableMetrics;
            }

            public boolean isEnableDetailedLogging() {
                return enableDetailedLogging;
            }

            public void setEnableDetailedLogging(boolean enableDetailedLogging) {
                this.enableDetailedLogging = enableDetailedLogging;
            }

            public int getCacheExpiration() {
                return cacheExpiration;
            }

            public void setCacheExpiration(int cacheExpiration) {
                this.cacheExpiration = cacheExpiration;
            }

            public int getMaxCacheSize() {
                return maxCacheSize;
            }

            public void setMaxCacheSize(int maxCacheSize) {
                this.maxCacheSize = maxCacheSize;
            }
        }

        // Main getters and setters
        public Processing getProcessing() {
            return processing;
        }

        public void setProcessing(Processing processing) {
            this.processing = processing;
        }

        public Performance getPerformance() {
            return performance;
        }

        public void setPerformance(Performance performance) {
            this.performance = performance;
        }
    }
}
