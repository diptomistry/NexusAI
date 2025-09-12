package com.example.ai_assistant_backend.controller;

import com.example.ai_assistant_backend.dto.AiChatRequest;
import com.example.ai_assistant_backend.dto.AiChatResponse;
import com.example.ai_assistant_backend.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AiChatController {

    @Autowired
    private AiChatService aiChatService;

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> generateResponse(@RequestBody AiChatRequest request) {
        try {
            AiChatResponse response = aiChatService.generateResponse(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            AiChatResponse errorResponse = new AiChatResponse("assistant",
                    "Sorry, I encountered an error processing your request. Please try again.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
