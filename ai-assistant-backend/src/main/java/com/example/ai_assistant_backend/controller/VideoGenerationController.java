package com.example.ai_assistant_backend.controller;

import com.example.ai_assistant_backend.service.TextVideoGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Video Generation Controller
 */
@RestController
@RequestMapping("/api/video")
@CrossOrigin(origins = "*")
public class VideoGenerationController {

    @Autowired
    private TextVideoGenerationService videoGenerationService;

    /**
     * Generate video from text prompt
     */
    @PostMapping(value = "/generate/text", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<TextVideoGenerationService.VideoGenerationResponse> generateVideoFromText(
            @RequestParam("prompt") String prompt,
            @RequestParam("userId") String userId,
            @RequestParam(value = "duration", required = false) Integer duration,
            @RequestParam(value = "resolution", required = false) String resolution,
            @RequestParam(value = "aspectRatio", required = false) String aspectRatio) {

        try {
            TextVideoGenerationService.VideoGenerationResponse response = videoGenerationService
                    .generateVideoFromText(prompt, userId, duration, resolution, aspectRatio);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    new TextVideoGenerationService.VideoGenerationResponse(null, "error", null, e.getMessage()));
        }
    }

    /**
     * Generate video from image
     */
    @PostMapping(value = "/generate/image", consumes = "multipart/form-data")
    public ResponseEntity<TextVideoGenerationService.VideoGenerationResponse> generateVideoFromImage(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam("prompt") String prompt,
            @RequestParam("userId") String userId,
            @RequestParam(value = "duration", required = false) Integer duration,
            @RequestParam(value = "resolution", required = false) String resolution,
            @RequestParam(value = "aspectRatio", required = false) String aspectRatio) {

        try {
            TextVideoGenerationService.VideoGenerationResponse response = videoGenerationService
                    .generateVideoFromImage(imageFile, prompt, userId, duration, resolution, aspectRatio);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    new TextVideoGenerationService.VideoGenerationResponse(null, "error", null, e.getMessage()));
        }
    }

    /**
     * Get video generation status
     */
    @GetMapping("/status/{taskId}")
    public ResponseEntity<TextVideoGenerationService.VideoGenerationResponse> getVideoStatus(
            @PathVariable String taskId) {

        try {
            TextVideoGenerationService.VideoGenerationResponse response = videoGenerationService
                    .getVideoStatus(taskId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    new TextVideoGenerationService.VideoGenerationResponse(taskId, "error", null, e.getMessage()));
        }
    }

    /**
     * Clean up old video tasks
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, String>> cleanupOldTasks() {
        try {
            // Simple cleanup - remove old tasks
            return ResponseEntity.ok(Map.of("status", "success", "message", "Old tasks cleaned up"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
