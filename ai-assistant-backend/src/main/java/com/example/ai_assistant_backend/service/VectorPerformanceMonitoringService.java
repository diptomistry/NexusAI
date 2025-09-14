package com.example.ai_assistant_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Advanced Performance Monitoring Service for Vector Operations
 * Demonstrates enterprise-level metrics collection and performance tracking
 */
@Service
public class VectorPerformanceMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(VectorPerformanceMonitoringService.class);

    // Metrics storage
    private final Map<String, OperationMetrics> operationMetrics = new ConcurrentHashMap<>();
    private final AtomicLong totalEmbeddingRequests = new AtomicLong(0);
    private final AtomicLong totalSearchRequests = new AtomicLong(0);
    private final AtomicLong totalChunkingOperations = new AtomicLong(0);

    /**
     * Start timing an operation
     */
    public PerformanceTimer startTimer(String operationType) {
        return new PerformanceTimer(operationType);
    }

    /**
     * Record embedding generation metrics
     */
    public void recordEmbeddingGeneration(String operationType, Duration duration, int textLength, boolean success) {
        totalEmbeddingRequests.incrementAndGet();

        OperationMetrics metrics = operationMetrics.computeIfAbsent(operationType, k -> new OperationMetrics());
        metrics.recordOperation(duration, success);

        if (logger.isDebugEnabled()) {
            logger.debug("Embedding generation - Operation: {}, Duration: {}ms, Text Length: {}, Success: {}",
                    operationType, duration.toMillis(), textLength, success);
        }

        // Log slow operations
        if (duration.toMillis() > 5000) {
            logger.warn("Slow embedding generation detected - Operation: {}, Duration: {}ms",
                    operationType, duration.toMillis());
        }
    }

    /**
     * Record vector search metrics
     */
    public void recordVectorSearch(String searchType, Duration duration, int resultCount, double avgSimilarity,
            boolean success) {
        totalSearchRequests.incrementAndGet();

        String operationType = "search_" + searchType;
        OperationMetrics metrics = operationMetrics.computeIfAbsent(operationType, k -> new OperationMetrics());
        metrics.recordOperation(duration, success);

        if (logger.isDebugEnabled()) {
            logger.debug("Vector search - Type: {}, Duration: {}ms, Results: {}, Avg Similarity: {:.3f}, Success: {}",
                    searchType, duration.toMillis(), resultCount, avgSimilarity, success);
        }

        // Log performance issues
        if (duration.toMillis() > 2000) {
            logger.warn("Slow vector search detected - Type: {}, Duration: {}ms",
                    searchType, duration.toMillis());
        }

        if (resultCount == 0) {
            logger.info("No results found for search - Type: {}, Duration: {}ms",
                    searchType, duration.toMillis());
        }
    }

    /**
     * Record document chunking metrics
     */
    public void recordChunkingOperation(String documentType, Duration duration, int originalLength,
            int chunkCount, boolean success) {
        totalChunkingOperations.incrementAndGet();

        String operationType = "chunking_" + documentType;
        OperationMetrics metrics = operationMetrics.computeIfAbsent(operationType, k -> new OperationMetrics());
        metrics.recordOperation(duration, success);

        if (logger.isDebugEnabled()) {
            logger.debug("Document chunking - Type: {}, Duration: {}ms, Original Length: {}, Chunks: {}, Success: {}",
                    documentType, duration.toMillis(), originalLength, chunkCount, success);
        }

        // Calculate chunking efficiency
        double avgChunkSize = chunkCount > 0 ? (double) originalLength / chunkCount : 0;

        if (avgChunkSize < 100 || avgChunkSize > 2000) {
            logger.warn("Suboptimal chunk size detected - Type: {}, Avg Chunk Size: {:.1f}",
                    documentType, avgChunkSize);
        }
    }

    /**
     * Record cache performance
     */
    public void recordCacheOperation(String cacheType, String operation, boolean hit) {
        String operationType = "cache_" + cacheType + "_" + operation;
        OperationMetrics metrics = operationMetrics.computeIfAbsent(operationType, k -> new OperationMetrics());

        if (hit) {
            metrics.recordCacheHit();
        } else {
            metrics.recordCacheMiss();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Cache operation - Type: {}, Operation: {}, Hit: {}", cacheType, operation, hit);
        }
    }

    /**
     * Get comprehensive performance report
     */
    public PerformanceReport getPerformanceReport() {
        PerformanceReport report = new PerformanceReport();

        // Overall statistics
        report.setTotalEmbeddingRequests(totalEmbeddingRequests.get());
        report.setTotalSearchRequests(totalSearchRequests.get());
        report.setTotalChunkingOperations(totalChunkingOperations.get());

        // Operation-specific metrics
        for (Map.Entry<String, OperationMetrics> entry : operationMetrics.entrySet()) {
            OperationMetrics metrics = entry.getValue();
            PerformanceReport.OperationSummary summary = new PerformanceReport.OperationSummary(
                    entry.getKey(),
                    metrics.getTotalOperations(),
                    metrics.getSuccessfulOperations(),
                    metrics.getFailedOperations(),
                    metrics.getAverageLatency(),
                    metrics.getMinLatency(),
                    metrics.getMaxLatency(),
                    metrics.getCacheHits(),
                    metrics.getCacheMisses());
            report.addOperationSummary(summary);
        }

        return report;
    }

    /**
     * Get health check status
     */
    public HealthStatus getHealthStatus() {
        HealthStatus status = new HealthStatus();

        // Check recent error rates
        long recentFailures = operationMetrics.values().stream()
                .mapToLong(OperationMetrics::getRecentFailures)
                .sum();

        long recentOperations = operationMetrics.values().stream()
                .mapToLong(OperationMetrics::getRecentOperations)
                .sum();

        double errorRate = recentOperations > 0 ? (double) recentFailures / recentOperations : 0.0;

        // Determine health status
        if (errorRate > 0.1) { // More than 10% error rate
            status.setStatus(HealthStatus.Status.UNHEALTHY);
            status.addIssue("High error rate: " + String.format("%.2f%%", errorRate * 100));
        } else if (errorRate > 0.05) { // More than 5% error rate
            status.setStatus(HealthStatus.Status.DEGRADED);
            status.addIssue("Elevated error rate: " + String.format("%.2f%%", errorRate * 100));
        } else {
            status.setStatus(HealthStatus.Status.HEALTHY);
        }

        // Check performance
        double avgLatency = operationMetrics.values().stream()
                .mapToDouble(OperationMetrics::getAverageLatency)
                .average()
                .orElse(0.0);

        if (avgLatency > 5000) { // More than 5 seconds average
            status.setStatus(HealthStatus.Status.DEGRADED);
            status.addIssue("High average latency: " + String.format("%.1fms", avgLatency));
        }

        return status;
    }

    /**
     * Reset all metrics (for testing or maintenance)
     */
    public void resetMetrics() {
        operationMetrics.clear();
        totalEmbeddingRequests.set(0);
        totalSearchRequests.set(0);
        totalChunkingOperations.set(0);
        logger.info("Performance metrics reset");
    }

    /**
     * Performance timer for measuring operation duration
     */
    public class PerformanceTimer {
        private final String operationType;
        private final Instant startTime;

        public PerformanceTimer(String operationType) {
            this.operationType = operationType;
            this.startTime = Instant.now();
        }

        public Duration stop() {
            return Duration.between(startTime, Instant.now());
        }

        public void stopAndRecord(boolean success) {
            Duration duration = stop();
            OperationMetrics metrics = operationMetrics.computeIfAbsent(operationType, k -> new OperationMetrics());
            metrics.recordOperation(duration, success);
        }
    }

    /**
     * Metrics storage for individual operations
     */
    private static class OperationMetrics {
        private final AtomicLong totalOperations = new AtomicLong(0);
        private final AtomicLong successfulOperations = new AtomicLong(0);
        private final AtomicLong failedOperations = new AtomicLong(0);
        private final AtomicLong totalLatency = new AtomicLong(0);
        private final AtomicLong minLatency = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxLatency = new AtomicLong(0);
        private final AtomicLong cacheHits = new AtomicLong(0);
        private final AtomicLong cacheMisses = new AtomicLong(0);

        // Recent metrics (for health checks)
        private final AtomicLong recentOperations = new AtomicLong(0);
        private final AtomicLong recentFailures = new AtomicLong(0);

        public void recordOperation(Duration duration, boolean success) {
            long latencyMs = duration.toMillis();

            totalOperations.incrementAndGet();
            recentOperations.incrementAndGet();
            totalLatency.addAndGet(latencyMs);

            // Update min/max latency
            minLatency.updateAndGet(current -> Math.min(current, latencyMs));
            maxLatency.updateAndGet(current -> Math.max(current, latencyMs));

            if (success) {
                successfulOperations.incrementAndGet();
            } else {
                failedOperations.incrementAndGet();
                recentFailures.incrementAndGet();
            }
        }

        public void recordCacheHit() {
            cacheHits.incrementAndGet();
        }

        public void recordCacheMiss() {
            cacheMisses.incrementAndGet();
        }

        // Getters
        public long getTotalOperations() {
            return totalOperations.get();
        }

        public long getSuccessfulOperations() {
            return successfulOperations.get();
        }

        public long getFailedOperations() {
            return failedOperations.get();
        }

        public long getCacheHits() {
            return cacheHits.get();
        }

        public long getCacheMisses() {
            return cacheMisses.get();
        }

        public long getRecentOperations() {
            return recentOperations.get();
        }

        public long getRecentFailures() {
            return recentFailures.get();
        }

        public double getAverageLatency() {
            long total = totalOperations.get();
            return total > 0 ? (double) totalLatency.get() / total : 0.0;
        }

        public long getMinLatency() {
            long min = minLatency.get();
            return min == Long.MAX_VALUE ? 0 : min;
        }

        public long getMaxLatency() {
            return maxLatency.get();
        }
    }

    /**
     * Performance report data structure
     */
    public static class PerformanceReport {
        private long totalEmbeddingRequests;
        private long totalSearchRequests;
        private long totalChunkingOperations;
        private final java.util.List<OperationSummary> operationSummaries = new java.util.ArrayList<>();

        // Getters and setters
        public long getTotalEmbeddingRequests() {
            return totalEmbeddingRequests;
        }

        public void setTotalEmbeddingRequests(long totalEmbeddingRequests) {
            this.totalEmbeddingRequests = totalEmbeddingRequests;
        }

        public long getTotalSearchRequests() {
            return totalSearchRequests;
        }

        public void setTotalSearchRequests(long totalSearchRequests) {
            this.totalSearchRequests = totalSearchRequests;
        }

        public long getTotalChunkingOperations() {
            return totalChunkingOperations;
        }

        public void setTotalChunkingOperations(long totalChunkingOperations) {
            this.totalChunkingOperations = totalChunkingOperations;
        }

        public java.util.List<OperationSummary> getOperationSummaries() {
            return operationSummaries;
        }

        public void addOperationSummary(OperationSummary summary) {
            operationSummaries.add(summary);
        }

        public static class OperationSummary {
            private final String operationType;
            private final long totalOperations;
            private final long successfulOperations;
            private final long failedOperations;
            private final double averageLatency;
            private final long minLatency;
            private final long maxLatency;
            private final long cacheHits;
            private final long cacheMisses;

            public OperationSummary(String operationType, long totalOperations, long successfulOperations,
                    long failedOperations, double averageLatency, long minLatency, long maxLatency,
                    long cacheHits, long cacheMisses) {
                this.operationType = operationType;
                this.totalOperations = totalOperations;
                this.successfulOperations = successfulOperations;
                this.failedOperations = failedOperations;
                this.averageLatency = averageLatency;
                this.minLatency = minLatency;
                this.maxLatency = maxLatency;
                this.cacheHits = cacheHits;
                this.cacheMisses = cacheMisses;
            }

            // Getters
            public String getOperationType() {
                return operationType;
            }

            public long getTotalOperations() {
                return totalOperations;
            }

            public long getSuccessfulOperations() {
                return successfulOperations;
            }

            public long getFailedOperations() {
                return failedOperations;
            }

            public double getAverageLatency() {
                return averageLatency;
            }

            public long getMinLatency() {
                return minLatency;
            }

            public long getMaxLatency() {
                return maxLatency;
            }

            public long getCacheHits() {
                return cacheHits;
            }

            public long getCacheMisses() {
                return cacheMisses;
            }

            public double getSuccessRate() {
                return totalOperations > 0 ? (double) successfulOperations / totalOperations : 0.0;
            }

            public double getCacheHitRate() {
                long totalCacheOperations = cacheHits + cacheMisses;
                return totalCacheOperations > 0 ? (double) cacheHits / totalCacheOperations : 0.0;
            }
        }
    }

    /**
     * Health status data structure
     */
    public static class HealthStatus {
        public enum Status {
            HEALTHY, DEGRADED, UNHEALTHY
        }

        private Status status = Status.HEALTHY;
        private final java.util.List<String> issues = new java.util.ArrayList<>();

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public java.util.List<String> getIssues() {
            return issues;
        }

        public void addIssue(String issue) {
            issues.add(issue);
        }

        public boolean isHealthy() {
            return status == Status.HEALTHY;
        }
    }
}
