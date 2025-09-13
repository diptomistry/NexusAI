package com.example.ai_assistant_backend.service;

import com.example.ai_assistant_backend.dto.DocumentDTO;
import com.example.ai_assistant_backend.dto.DocumentUploadResponse;
import com.example.ai_assistant_backend.model.Document;
import com.example.ai_assistant_backend.repository.DocumentRepository;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    private final Tika tika = new Tika();
    private final String uploadDir = "uploads/documents";

    // Supported file types
    private final Set<String> SUPPORTED_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain",
            "text/markdown",
            "application/rtf",
            "text/html",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    public DocumentUploadResponse uploadDocument(MultipartFile file, String userId, String assistantId) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return new DocumentUploadResponse("File is empty", false);
            }

            String contentType = file.getContentType();
            if (contentType == null || !SUPPORTED_TYPES.contains(contentType)) {
                return new DocumentUploadResponse(
                        "Unsupported file type. Supported types: PDF, Word, Text, Markdown, RTF, HTML, Excel", false);
            }

            // Check file size (max 10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return new DocumentUploadResponse("File size too large. Maximum size is 10MB", false);
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFileName = file.getOriginalFilename();
            String fileName = System.currentTimeMillis() + "_" + originalFileName;
            Path filePath = uploadPath.resolve(fileName);

            // Save file to disk
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Extract text from file
            String extractedText = extractTextFromFile(file);

            // Save document metadata to database
            Document document = new Document(
                    fileName,
                    originalFileName,
                    contentType,
                    file.getSize(),
                    extractedText,
                    userId,
                    assistantId,
                    filePath.toString());

            Document savedDocument = documentRepository.save(document);

            return new DocumentUploadResponse(
                    savedDocument.getId(),
                    savedDocument.getFileName(),
                    savedDocument.getOriginalFileName(),
                    savedDocument.getFileType(),
                    savedDocument.getFileSize(),
                    "Document uploaded successfully",
                    true);

        } catch (Exception e) {
            e.printStackTrace();
            return new DocumentUploadResponse("Failed to upload document: " + e.getMessage(), false);
        }
    }

    private String extractTextFromFile(MultipartFile file) throws IOException, TikaException {
        try (InputStream inputStream = file.getInputStream()) {
            String extractedText = tika.parseToString(inputStream);

            // Clean and normalize the text
            extractedText = extractedText.trim();
            extractedText = extractedText.replaceAll("\\s+", " "); // Replace multiple spaces with single space

            // Limit text length to prevent very large documents from causing issues
            if (extractedText.length() > 50000) {
                extractedText = extractedText.substring(0, 50000) + "... [Content truncated]";
            }

            return extractedText;
        }
    }

    public List<DocumentDTO> getUserDocuments(String userId) {
        List<Document> documents = documentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return documents.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DocumentDTO> getAssistantDocuments(String userId, String assistantId) {
        List<Document> documents = documentRepository.findDocumentsForAssistant(userId, assistantId);
        return documents.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public boolean deleteDocument(Long documentId, String userId) {
        Optional<Document> documentOpt = documentRepository.findById(documentId);
        if (documentOpt.isPresent() && documentOpt.get().getUserId().equals(userId)) {
            Document document = documentOpt.get();

            // Delete file from disk
            try {
                Path filePath = Paths.get(document.getFilePath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Failed to delete file from disk: " + e.getMessage());
            }

            // Delete from database
            documentRepository.delete(document);
            return true;
        }
        return false;
    }

    public String getRelevantDocumentContext(String userId, String assistantId, String userQuery) {
        List<String> documentTexts = documentRepository.findExtractedTextForAssistant(userId, assistantId);

        if (documentTexts.isEmpty()) {
            return "";
        }

        // For simple implementation, concatenate all document texts
        // In a more advanced implementation, you could use semantic search
        StringBuilder context = new StringBuilder();
        context.append("Based on the following documents:\n\n");

        for (int i = 0; i < documentTexts.size(); i++) {
            String text = documentTexts.get(i);
            if (text != null && !text.trim().isEmpty()) {
                // Get most relevant parts of the document (first 1000 chars as a simple
                // approach)
                String relevantPart = text.length() > 1000 ? text.substring(0, 1000) + "..." : text;
                context.append("Document ").append(i + 1).append(":\n");
                context.append(relevantPart).append("\n\n");
            }
        }

        return context.toString();
    }

    public long getUserDocumentCount(String userId) {
        return documentRepository.countByUserId(userId);
    }

    private DocumentDTO convertToDTO(Document document) {
        return new DocumentDTO(
                document.getId(),
                document.getFileName(),
                document.getOriginalFileName(),
                document.getFileType(),
                document.getFileSize(),
                document.getAssistantId(),
                document.getCreatedAt(),
                document.getUpdatedAt());
    }
}
