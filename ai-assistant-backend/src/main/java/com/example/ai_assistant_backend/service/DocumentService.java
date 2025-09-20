package com.example.ai_assistant_backend.service;

import com.example.ai_assistant_backend.dto.DocumentDTO;
import com.example.ai_assistant_backend.dto.DocumentUploadResponse;
import com.example.ai_assistant_backend.model.Document;
import com.example.ai_assistant_backend.repository.DocumentRepository;
import com.example.ai_assistant_backend.repository.DocumentChunkRepository;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    private final Tika tika;

    public DocumentService() {
        // Configure Tika with more aggressive settings for better PDF text extraction
        this.tika = new Tika();
        // Set maximum text length to 10MB to handle very large documents
        this.tika.setMaxStringLength(10 * 1024 * 1024);
    }

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

            // Check document limit (max 2 documents per assistant)
            if (assistantId != null && !assistantId.trim().isEmpty()) {
                long documentCount = documentRepository.countByUserIdAndAssistantId(userId, assistantId);
                if (documentCount >= 2) {
                    return new DocumentUploadResponse(
                            "Maximum 2 documents allowed per assistant. Please delete a document before uploading a new one.",
                            false);
                }
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

            // Advanced text cleaning and normalization
            extractedText = cleanAndNormalizeText(extractedText);

            // Log document size for monitoring
            logger.info("Extracted text length: {} characters from file: {}",
                    extractedText.length(), file.getOriginalFilename());

            // Only truncate if document is extremely large (over 1MB of text)
            // This allows for much larger documents while preventing memory issues
            if (extractedText.length() > 1000000) {
                logger.warn("Document text exceeds 1MB limit, truncating to prevent memory issues");
                extractedText = extractedText.substring(0, 1000000) + "... [Content truncated - document too large]";
            }

            return extractedText;
        }
    }

    /**
     * Advanced text cleaning and normalization to fix PDF extraction artifacts
     */
    private String cleanAndNormalizeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // Step 1: Basic cleanup
        text = text.trim();

        // Step 2: Fix common PDF extraction issues
        // Fix word splitting (e.g., "base d" -> "based", "an d" -> "and")
        text = fixWordSplitting(text);

        // Fix specific common OCR errors
        text = text.replaceAll("\\bfab\\s+rics\\b", "fabrics");
        text = text.replaceAll("\\bper\\s+forman\\s+ce\\b", "performance");
        text = text.replaceAll("\\bmea\\s+sure\\s+ment\\b", "measurement");
        text = text.replaceAll("\\bmon\\s+itor\\s+ing\\b", "monitoring");
        text = text.replaceAll("\\bstruc\\s+tur\\s+al\\b", "structural");
        text = text.replaceAll("\\btech\\s+nol\\s+ogy\\b", "technology");
        text = text.replaceAll("\\bqual\\s+ity\\b", "quality");
        text = text.replaceAll("\\bman\\s+u\\s+fac\\s+tur\\s+ing\\b", "manufacturing");

        // Fix more specific word splitting patterns from the document
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
        text = text.replaceAll("\\bcharac\\s+terization\\b", "characterization");
        text = text.replaceAll("\\bdeploy\\s+ment\\b", "deployment");
        text = text.replaceAll("\\bmoni\\s+toring\\b", "monitoring");
        text = text.replaceAll("\\bmea\\s+surement\\b", "measurement");
        text = text.replaceAll("\\bappar\\s+atus\\b", "apparatus");
        text = text.replaceAll("\\bcharac\\s+teristics\\b", "characteristics");
        text = text.replaceAll("\\bperfor\\s+mance\\b", "performance");
        text = text.replaceAll("\\bmanu\\s+facturing\\b", "manufacturing");
        text = text.replaceAll("\\bstruc\\s+tural\\b", "structural");
        text = text.replaceAll("\\btech\\s+nology\\b", "technology");
        text = text.replaceAll("\\bqual\\s+ity\\b", "quality");
        text = text.replaceAll("\\bcon\\s+trol\\b", "control");
        text = text.replaceAll("\\bwear\\s+able\\b", "wearable");
        text = text.replaceAll("\\bmem\\s+brane\\b", "membrane");
        text = text.replaceAll("\\bpro\\s+tective\\b", "protective");
        text = text.replaceAll("\\bap\\s+parel\\b", "apparel");
        text = text.replaceAll("\\brecom\\s+mendations\\b", "recommendations");
        text = text.replaceAll("\\brobust\\s+ness\\b", "robustness");
        text = text.replaceAll("\\bus\\s+ability\\b", "usability");

        // Fix vacuum sealer specific patterns
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

        // Step 3: Fix markdown formatting issues
        // Fix bold text formatting (ensure proper spacing)
        text = text.replaceAll("\\*\\*\\s*([^\\*]+)\\s*\\*\\*", "**$1**");
        text = text.replaceAll("\\*\\*([^\\s][^\\*]*[^\\s])\\*\\*", "**$1**");

        // Step 4: Normalize whitespace
        text = text.replaceAll("\\s+", " "); // Replace multiple spaces with single space
        text = text.replaceAll("\\n\\s*\\n", "\n\n"); // Normalize paragraph breaks
        text = text.replaceAll("\\n+", "\n"); // Remove excessive line breaks

        // Step 5: Fix punctuation spacing
        text = text.replaceAll("\\s+([.!?])", "$1"); // Remove spaces before punctuation
        text = text.replaceAll("([.!?])\\s*([A-Z])", "$1 $2"); // Ensure space after sentence endings

        // Step 6: Clean up bullet points and lists
        text = text.replaceAll("\\n\\s*[-•*]\\s+", "\n- "); // Normalize bullet points
        text = text.replaceAll("\\n\\s*\\d+\\.\\s+", "\n1. "); // Normalize numbered lists

        return text.trim();
    }

    /**
     * Fix word splitting issues from PDF extraction
     */
    private String fixWordSplitting(String text) {
        // More aggressive pattern to catch word splits
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b([a-zA-Z]{2,})\\s+([a-zA-Z]{2,})\\b");
        java.util.regex.Matcher matcher = pattern.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String firstWord = matcher.group(1);
            String secondWord = matcher.group(2);
            String combined = firstWord + secondWord;

            // More lenient word detection for technical terms
            if (isLikelyWord(combined) || isTechnicalTerm(combined)) {
                matcher.appendReplacement(result, combined);
            } else {
                matcher.appendReplacement(result, matcher.group(0));
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Check if a word looks like a technical term
     */
    private boolean isTechnicalTerm(String word) {
        if (word.length() < 4)
            return false;

        // Check for common technical suffixes
        boolean hasTechnicalSuffix = word.matches(
                ".*(tion|sion|ing|ment|ness|ity|ism|logy|graphy|metry|scopy|physis|genesis|analysis|synthesis).*");

        // Check for common technical prefixes
        boolean hasTechnicalPrefix = word.matches(
                "(micro|macro|multi|inter|intra|trans|sub|super|hyper|ultra|pseudo|semi|auto|bio|geo|hydro|electro|thermo|photo|mechano|electro).*");

        // Check for compound technical words
        boolean isCompound = word.matches(
                ".*(stress|strain|fabric|textile|sensor|monitor|measure|analy|character|perform|manufac|technol|qual|struct|appar|robust|usab).*");

        return hasTechnicalSuffix || hasTechnicalPrefix || isCompound;
    }

    /**
     * Check if a combined word looks like a real word
     */
    private boolean isLikelyWord(String word) {
        if (word.length() < 3)
            return false;

        // Check if it has both vowels and consonants
        boolean hasVowel = word.matches(".*[aeiouAEIOU].*");
        boolean hasConsonant = word.matches(".*[bcdfghjklmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ].*");

        // Check for common word patterns
        boolean hasCommonPattern = word.matches(".*(ing|ed|er|ly|tion|sion|ness|ment|able|ible).*");

        return hasVowel && hasConsonant && (hasCommonPattern || word.length() > 4);
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

    @Transactional
    public boolean deleteDocument(Long documentId, String userId) {
        Optional<Document> documentOpt = documentRepository.findById(documentId);
        if (documentOpt.isPresent() && documentOpt.get().getUserId().equals(userId)) {
            Document document = documentOpt.get();

            // Delete associated document chunks first to avoid foreign key constraint
            // violation
            try {
                documentChunkRepository.deleteByDocumentId(documentId);
                logger.info("Deleted document chunks for document ID: {}", documentId);
            } catch (Exception e) {
                logger.error("Failed to delete document chunks for document {}: {}", documentId, e.getMessage(), e);
                throw new RuntimeException("Failed to delete document chunks: " + e.getMessage(), e);
            }

            // Delete file from disk
            try {
                Path filePath = Paths.get(document.getFilePath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                logger.warn("Failed to delete file from disk: {}", e.getMessage());
                // Don't fail the entire operation if file deletion fails
            }

            // Delete from database
            documentRepository.delete(document);
            logger.info("Successfully deleted document ID: {}", documentId);
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
                // Clean the text before sending to AI to fix any remaining formatting issues
                String cleanedText = cleanAndNormalizeText(text);

                // Get most relevant parts of the document (first 1000 chars as a simple
                // approach)
                String relevantPart = cleanedText.length() > 1000 ? cleanedText.substring(0, 1000) + "..."
                        : cleanedText;
                context.append("Document ").append(i + 1).append(":\n");
                context.append(relevantPart).append("\n\n");
            }
        }

        return context.toString();
    }

    public long getUserDocumentCount(String userId) {
        return documentRepository.countByUserId(userId);
    }

    public Map<String, Object> getDocumentDebugInfo(Long documentId) {
        try {
            Document document = documentRepository.findById(documentId).orElse(null);
            if (document == null) {
                Map<String, Object> errorInfo = new HashMap<>();
                errorInfo.put("error", "Document not found");
                return errorInfo;
            }

            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("id", document.getId());
            debugInfo.put("originalFileName", document.getOriginalFileName());
            debugInfo.put("fileSize", document.getFileSize());
            debugInfo.put("fileType", document.getFileType());

            String extractedText = document.getExtractedText();
            if (extractedText != null) {
                debugInfo.put("extractedTextLength", extractedText.length());
                debugInfo.put("extractedTextPreview",
                        extractedText.substring(0, Math.min(500, extractedText.length())));
            } else {
                debugInfo.put("extractedTextLength", 0);
                debugInfo.put("extractedTextPreview", "No text");
            }

            debugInfo.put("createdAt", document.getCreatedAt());

            return debugInfo;
        } catch (Exception e) {
            logger.error("Error getting document debug info: {}", e.getMessage(), e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", "Failed to get document info: " + e.getMessage());
            return errorInfo;
        }
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
