package com.example.ai_assistant_backend.controller;

import com.example.ai_assistant_backend.dto.*;
import com.example.ai_assistant_backend.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@CrossOrigin(origins = "*") // Configure this properly for production
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @PostMapping
    public ResponseEntity<ConversationDTO> createConversation(@RequestBody CreateConversationRequest request) {
        try {
            ConversationDTO conversation = conversationService.createConversation(request);
            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ConversationDTO>> getUserConversations(@PathVariable String userId) {
        try {
            List<ConversationDTO> conversations = conversationService.getUserConversations(userId);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}/assistant/{assistantId}")
    public ResponseEntity<List<ConversationDTO>> getUserConversationsByAssistant(
            @PathVariable String userId,
            @PathVariable String assistantId) {
        try {
            List<ConversationDTO> conversations = conversationService
                    .getUserConversationsByAssistant(userId, assistantId);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationDTO> getConversation(@PathVariable Long conversationId) {
        try {
            ConversationDTO conversation = conversationService.getConversationWithMessages(conversationId);
            return ResponseEntity.ok(conversation);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/messages")
    public ResponseEntity<MessageDTO> addMessage(@RequestBody AddMessageRequest request) {
        try {
            MessageDTO message = conversationService.addMessage(request);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long conversationId) {
        try {
            conversationService.deleteConversation(conversationId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
