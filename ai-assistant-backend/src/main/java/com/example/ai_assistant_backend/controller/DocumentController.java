package com.example.ai_assistant_backend.controller;

import com.example.ai_assistant_backend.dto.DocumentDTO;
import com.example.ai_assistant_backend.dto.DocumentUploadResponse;
import com.example.ai_assistant_backend.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam(value = "assistantId", required = false) String assistantId) {

        try {
            DocumentUploadResponse response = documentService.uploadDocument(file, userId, assistantId);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new DocumentUploadResponse("Failed to upload document: " + e.getMessage(), false));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DocumentDTO>> getUserDocuments(@PathVariable String userId) {
        try {
            List<DocumentDTO> documents = documentService.getUserDocuments(userId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/assistant/{userId}/{assistantId}")
    public ResponseEntity<List<DocumentDTO>> getAssistantDocuments(
            @PathVariable String userId,
            @PathVariable String assistantId) {
        try {
            List<DocumentDTO> documents = documentService.getAssistantDocuments(userId, assistantId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<String> deleteDocument(
            @PathVariable Long documentId,
            @RequestParam("userId") String userId) {
        try {
            boolean deleted = documentService.deleteDocument(documentId, userId);

            if (deleted) {
                return ResponseEntity.ok("Document deleted successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to delete document: " + e.getMessage());
        }
    }

    @GetMapping("/count/{userId}")
    public ResponseEntity<Long> getUserDocumentCount(@PathVariable String userId) {
        try {
            long count = documentService.getUserDocumentCount(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/debug/{documentId}")
    public ResponseEntity<Map<String, Object>> getDocumentDebugInfo(@PathVariable Long documentId) {
        try {
            Map<String, Object> debugInfo = documentService.getDocumentDebugInfo(documentId);
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/context")
    public ResponseEntity<String> getDocumentContext(
            @RequestParam("userId") String userId,
            @RequestParam("assistantId") String assistantId,
            @RequestParam("query") String query) {
        try {
            String context = documentService.getRelevantDocumentContext(userId, assistantId, query);
            return ResponseEntity.ok(context);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("");
        }
    }
}
