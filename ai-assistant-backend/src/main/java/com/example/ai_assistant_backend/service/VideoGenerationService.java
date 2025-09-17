package com.example.ai_assistant_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Video Generation Service using Seedance-1-Pro model via Replicate
 */
@Service
public class VideoGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(VideoGenerationService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${replicate.api.key}")
    private String replicateApiKey;

    // Store video generation tasks
    private final Map<String, VideoGenerationTask> videoTasks = new ConcurrentHashMap<>();

    /**
     * Start video generation from text prompt
     */
    public VideoGenerationResponse generateVideoFromText(
            String prompt,
            String userId,
            Integer duration,
            String resolution,
            String aspectRatio) {

        String taskId = UUID.randomUUID().toString();

        // Set defaults
        duration = duration != null ? duration : 5;
        resolution = resolution != null ? resolution : "1080p";
        aspectRatio = aspectRatio != null ? aspectRatio : "16:9";

        // Create task
        VideoGenerationTask task = new VideoGenerationTask(
                taskId, userId, "text", prompt, null, duration, resolution, aspectRatio);
        videoTasks.put(taskId, task);

        // Start async generation
        CompletableFuture.runAsync(() -> {
            try {
                generateVideoAsync(task);
            } catch (Exception e) {
                logger.error("Video generation failed for task {}: {}", taskId, e.getMessage(), e);
                task.setStatus("failed");
                task.setError(e.getMessage());
            }
        });

        return new VideoGenerationResponse(taskId, "processing", null, null);
    }

    /**
     * Start video generation from image
     */
    public VideoGenerationResponse generateVideoFromImage(
            MultipartFile imageFile,
            String prompt,
            String userId,
            Integer duration,
            String resolution,
            String aspectRatio) {

        String taskId = UUID.randomUUID().toString();

        // Set defaults
        duration = duration != null ? duration : 5;
        resolution = resolution != null ? resolution : "1080p";
        aspectRatio = aspectRatio != null ? aspectRatio : "16:9";

        // Create task
        VideoGenerationTask task = new VideoGenerationTask(
                taskId, userId, "image", prompt, imageFile, duration, resolution, aspectRatio);
        videoTasks.put(taskId, task);

        // Start async generation
        CompletableFuture.runAsync(() -> {
            try {
                generateVideoAsync(task);
            } catch (Exception e) {
                logger.error("Video generation failed for task {}: {}", taskId, e.getMessage(), e);
                task.setStatus("failed");
                task.setError(e.getMessage());
            }
        });

        return new VideoGenerationResponse(taskId, "processing", null, null);
    }

    /**
     * Get video generation status
     */
    public VideoGenerationResponse getVideoStatus(String taskId) {
        VideoGenerationTask task = videoTasks.get(taskId);
        if (task == null) {
            return new VideoGenerationResponse(taskId, "not_found", null, null);
        }

        return new VideoGenerationResponse(
                taskId,
                task.getStatus(),
                task.getVideoUrl(),
                task.getError());
    }

    /**
     * Async video generation
     */
    private void generateVideoAsync(VideoGenerationTask task) throws Exception {
        logger.info("Starting video generation for task: {}", task.getTaskId());

        // Prepare request body
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> input = new HashMap<>();

        input.put("prompt", task.getPrompt());
        input.put("duration", task.getDuration());
        input.put("resolution", task.getResolution());
        input.put("aspect_ratio", task.getAspectRatio());

        // Text-to-video only - no image handling

        // Use the correct Replicate API format for seedance-1-pro
        requestBody.put("input", input);

        // Call Replicate API with model in URL
        String url = "https://api.replicate.com/v1/models/bytedance/seedance-1-pro/predictions";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + replicateApiKey);
        headers.set("Content-Type", "application/json");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        // Parse response
        Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
        String predictionId = (String) responseMap.get("id");

        logger.info("Video generation started with prediction ID: {}", predictionId);

        // Poll for completion
        pollVideoGeneration(predictionId, task);
    }

    /**
     * Poll video generation status
     */
    private void pollVideoGeneration(String predictionId, VideoGenerationTask task) {
        String statusUrl = "https://api.replicate.com/v1/predictions/" + predictionId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + replicateApiKey);

        while (true) {
            try {
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.exchange(statusUrl, HttpMethod.GET, entity,
                        String.class);

                Map<String, Object> statusMap = objectMapper.readValue(response.getBody(), Map.class);
                String status = (String) statusMap.get("status");

                logger.info("Video generation status for task {}: {}", task.getTaskId(), status);

                if ("succeeded".equals(status)) {
                    // Get video URL
                    Object output = statusMap.get("output");
                    if (output instanceof String) {
                        task.setStatus("completed");
                        task.setVideoUrl((String) output);
                        logger.info("Video generation completed for task: {}", task.getTaskId());
                    } else if (output instanceof java.util.List) {
                        java.util.List<?> outputList = (java.util.List<?>) output;
                        if (!outputList.isEmpty() && outputList.get(0) instanceof String) {
                            task.setStatus("completed");
                            task.setVideoUrl((String) outputList.get(0));
                            logger.info("Video generation completed for task: {}", task.getTaskId());
                        } else {
                            task.setStatus("failed");
                            task.setError("No valid output URL received");
                        }
                    } else {
                        task.setStatus("failed");
                        task.setError("Unexpected output format: " + output.getClass().getSimpleName());
                    }
                    break;
                } else if ("failed".equals(status)) {
                    Object errorObj = statusMap.get("error");
                    String errorMessage = "Video generation failed";

                    if (errorObj instanceof String) {
                        errorMessage = (String) errorObj;
                    } else if (errorObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> error = (Map<String, Object>) errorObj;
                        errorMessage = (String) error.get("message");
                    }

                    task.setStatus("failed");
                    task.setError(errorMessage);
                    logger.error("Video generation failed for task {}: {}", task.getTaskId(), errorMessage);
                    break;
                }

                // Wait before next poll
                Thread.sleep(2000);

            } catch (Exception e) {
                logger.error("Error polling video generation status: {}", e.getMessage(), e);
                task.setStatus("failed");
                task.setError("Error checking generation status: " + e.getMessage());
                break;
            }
        }
    }

    /**
     * Get the latest version of a Replicate model
     */
    private String getModelVersion(String modelName) {
        try {
            String url = "https://api.replicate.com/v1/models/" + modelName;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + replicateApiKey);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            Map<String, Object> modelInfo = objectMapper.readValue(response.getBody(), Map.class);
            List<Map<String, Object>> versions = (List<Map<String, Object>>) modelInfo.get("latest_version");

            if (versions != null && !versions.isEmpty()) {
                return (String) versions.get(0).get("id");
            }

            // Fallback to a known working version
            return "latest";

        } catch (Exception e) {
            logger.warn("Failed to get model version for {}, using latest", modelName, e);
            return "latest";
        }
    }

    /**
     * Upload image to Replicate
     */
    private String uploadImageToReplicate(MultipartFile imageFile) throws IOException {
        try {
            // For now, let's use a simple approach - return a data URL
            // This will work for video generation without needing Supabase upload
            logger.info("Converting image to data URL for video generation: filename={}, size={} bytes",
                    imageFile.getOriginalFilename(), imageFile.getSize());

            // Convert image to base64 data URL
            String base64Image = java.util.Base64.getEncoder().encodeToString(imageFile.getBytes());
            String contentType = imageFile.getContentType();
            String dataUrl = "data:" + contentType + ";base64," + base64Image;

            logger.info("Image converted to data URL successfully");
            return dataUrl;

        } catch (Exception e) {
            logger.error("Error converting image to data URL: {}", e.getMessage(), e);
            // Fallback to placeholder
            return "https://via.placeholder.com/512x512/000000/FFFFFF?text=Convert+Error";
        }
    }

    /**
     * Clean up old tasks
     */
    public void cleanupOldTasks() {
        long cutoffTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000); // 24 hours
        videoTasks.entrySet().removeIf(entry -> entry.getValue().getCreatedAt() < cutoffTime);
    }

    // Inner classes
    public static class VideoGenerationTask {
        private final String taskId;
        private final String userId;
        private final String type; // "text" or "image"
        private final String prompt;
        private final MultipartFile imageFile;
        private final Integer duration;
        private final String resolution;
        private final String aspectRatio;
        private final long createdAt;

        private String status = "processing";
        private String videoUrl;
        private String error;

        public VideoGenerationTask(String taskId, String userId, String type, String prompt,
                MultipartFile imageFile, Integer duration, String resolution, String aspectRatio) {
            this.taskId = taskId;
            this.userId = userId;
            this.type = type;
            this.prompt = prompt;
            this.imageFile = imageFile;
            this.duration = duration;
            this.resolution = resolution;
            this.aspectRatio = aspectRatio;
            this.createdAt = System.currentTimeMillis();
        }

        // Getters and setters
        public String getTaskId() {
            return taskId;
        }

        public String getUserId() {
            return userId;
        }

        public String getType() {
            return type;
        }

        public String getPrompt() {
            return prompt;
        }

        public MultipartFile getImageFile() {
            return imageFile;
        }

        public Integer getDuration() {
            return duration;
        }

        public String getResolution() {
            return resolution;
        }

        public String getAspectRatio() {
            return aspectRatio;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getVideoUrl() {
            return videoUrl;
        }

        public void setVideoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }

    public static class VideoGenerationResponse {
        private final String taskId;
        private final String status;
        private final String videoUrl;
        private final String error;

        public VideoGenerationResponse(String taskId, String status, String videoUrl, String error) {
            this.taskId = taskId;
            this.status = status;
            this.videoUrl = videoUrl;
            this.error = error;
        }

        // Getters
        public String getTaskId() {
            return taskId;
        }

        public String getStatus() {
            return status;
        }

        public String getVideoUrl() {
            return videoUrl;
        }

        public String getError() {
            return error;
        }
    }
}
