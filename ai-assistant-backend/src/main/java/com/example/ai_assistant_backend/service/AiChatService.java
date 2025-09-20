package com.example.ai_assistant_backend.service;

import com.example.ai_assistant_backend.dto.AiChatRequest;
import com.example.ai_assistant_backend.dto.AiChatResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AiChatService {

    @Value("${replicate.api.key:}")
    private String replicateApiKey;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private RetrievalAugmentedGenerationService ragService;

    public AiChatService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public AiChatResponse generateResponse(AiChatRequest request) {
        try {
            String provider = request.getProvider();
            String userInput = request.getUserInput();
            String assistantInstruction = request.getAssistantInstruction();
            String userId = request.getUserId();
            String assistantId = request.getAssistantId();

            System.out.println("Received request for provider: " + provider);
            System.out.println("User input: " + userInput);
            System.out.println("Assistant instruction: " + assistantInstruction);

            // Get document context using RAG if user and assistant are provided
            String documentContext = "";
            if (userId != null && assistantId != null) {
                try {
                    // Use RAG service for intelligent document context retrieval
                    RetrievalAugmentedGenerationService.RAGConfig config = new RetrievalAugmentedGenerationService.RAGConfig();

                    // Special handling for "last part" queries - be more inclusive
                    if (userInput.toLowerCase().contains("last part") || userInput.toLowerCase().contains("conclusion")
                            || userInput.toLowerCase().contains("end")) {
                        config.setMaxChunks(50); // Get even more chunks for "last part" queries (supports 3-4
                                                 // documents)
                        config.setSimilarityThreshold(0.2); // Very low threshold to include all chunks
                        config.setMaxContextLength(25000); // More context for comprehensive responses
                    } else {
                        config.setMaxChunks(40); // Increased to support 3-4 documents (10-15 chunks each)
                        config.setSimilarityThreshold(0.25); // Lowered to include more chunks from multiple documents
                        config.setMaxContextLength(20000); // Increased to accommodate more documents
                    }

                    config.setIncludeMetadata(true); // Include metadata for better context
                    config.setApplyRecencyBoost(true); // Boost recent documents
                    config.setEnableDiversityFiltering(true); // Enable diversity filtering for better results

                    RetrievalAugmentedGenerationService.RAGContext ragContext = ragService.generateContext(
                            userInput,
                            UUID.fromString(userId),
                            assistantId,
                            config);

                    documentContext = ragContext.getContextText();

                } catch (Exception e) {
                    System.err
                            .println("RAG context retrieval failed, falling back to simple context: " + e.getMessage());
                    // Fallback to simple document context if RAG fails
                    documentContext = documentService.getRelevantDocumentContext(userId, assistantId, userInput);
                    System.out.println(
                            "Fallback document context retrieved: " + (documentContext.length() > 0 ? "Yes" : "No"));
                }
            }

            // Check if it's a Gemini model - only route to Gemini for the specific Gemini
            // model
            if (provider != null && provider.equals("google/gemini-2.0-flash")) {
                String fullInput = buildFullInput(userInput, assistantInstruction, documentContext);
                return generateGeminiResponse(fullInput, request.getAiResp());
            } else {
                // Use Replicate for all other models (OpenAI, Mistral, Anthropic, image models,
                // etc.)
                return generateReplicateResponse(provider, userInput, assistantInstruction, documentContext);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new AiChatResponse("assistant",
                    "Sorry, I encountered an error processing your request. Please try again.");
        }
    }

    private String buildFullInput(String userInput, String assistantInstruction, String documentContext) {
        StringBuilder fullInput = new StringBuilder();

        // Add document context first if available
        if (documentContext != null && !documentContext.trim().isEmpty()) {
            fullInput.append(documentContext).append("\n\n");
        }

        // Add formatting instructions to prevent word concatenation
        fullInput.append("IMPORTANT: When responding, ensure proper spacing between words. " +
                "Do not concatenate words together. Use proper punctuation and spacing. " +
                "For example, write 'elastic modulus' not 'elasticmodulus', " +
                "'maximum stress' not 'maximumstress', etc.\n\n");

        // Add user input
        fullInput.append(userInput);

        // Add assistant instruction as context
        if (assistantInstruction != null && !assistantInstruction.isEmpty()) {
            fullInput.append(":-").append(assistantInstruction);
        }

        return fullInput.toString();
    }

    private String buildFullPromptForModel(String input, String assistantInstruction, String documentContext) {
        StringBuilder fullPrompt = new StringBuilder();

        // Add document context first if available
        if (documentContext != null && !documentContext.trim().isEmpty()) {
            fullPrompt.append("Context from uploaded documents:\n");
            fullPrompt.append(documentContext).append("\n\n");
        }

        // Add assistant instruction
        if (assistantInstruction != null && !assistantInstruction.isEmpty()) {
            fullPrompt.append("Instructions: ").append(assistantInstruction).append("\n\n");
        }

        // Add formatting instructions to prevent word concatenation
        fullPrompt.append("IMPORTANT: When responding, ensure proper spacing between words. " +
                "Do not concatenate words together. Use proper punctuation and spacing. " +
                "For example, write 'elastic modulus' not 'elasticmodulus', " +
                "'maximum stress' not 'maximumstress', etc.\n\n");

        // Add user input
        fullPrompt.append("User: ").append(input).append("\nAssistant:");

        return fullPrompt.toString();
    }

    private AiChatResponse generateReplicateResponse(String model, String input, String assistantInstruction,
            String documentContext) {
        if (replicateApiKey == null || replicateApiKey.isEmpty()) {
            return new AiChatResponse("assistant",
                    "AI service is not configured properly. Please check the server configuration.");
        }

        if (model == null || model.isEmpty()) {
            return new AiChatResponse("assistant",
                    "AI model not specified. Please select an AI model.");
        }

        try {
            System.out.println("=== REPLICATE API CALL ===");
            System.out.println("Making Replicate API call for model: " + model);
            System.out.println("User input: " + input);
            System.out.println("Assistant instruction: " + assistantInstruction);
            System.out.println("Document context length: " + (documentContext != null ? documentContext.length() : 0));
            System.out.println("Model version: " + getModelVersion(model));

            // Step 1: Create a prediction
            String url = "https://api.replicate.com/v1/predictions";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + replicateApiKey);
            headers.set("Content-Type", "application/json");

            // Build request body for Replicate API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("version", getModelVersion(model));

            Map<String, Object> inputParams = new HashMap<>();

            // Try to extract any image URLs from the input text to support image models
            List<String> imageUrls = extractImageUrls(input);
            System.out.println("[AI] Extracted image URLs: " + imageUrls);

            // Default prompt param
            inputParams.put("prompt", input);

            // Model-specific parameter handling
            if (model.contains("deepseek")) {
                // DeepSeek models might use different parameters
                String fullPrompt = buildFullPromptForModel(input, assistantInstruction, documentContext);
                inputParams.put("prompt", fullPrompt);
                inputParams.put("max_tokens", 4096);
                inputParams.put("temperature", 0.7);
            } else if (model.contains("anthropic")) {
                // Anthropic models (Claude)
                String systemPrompt = "You are a helpful assistant.";
                if (assistantInstruction != null && !assistantInstruction.isEmpty()) {
                    systemPrompt = assistantInstruction;
                }

                // Add document context to system prompt if available
                if (documentContext != null && !documentContext.trim().isEmpty()) {
                    systemPrompt += "\n\nRelevant document context:\n" + documentContext;
                }

                inputParams.put("system_prompt", systemPrompt);
                inputParams.put("max_tokens", 4096);
                inputParams.put("temperature", 0.7);

            } else if (model.contains("openai")) {
                // OpenAI models
                String systemPrompt = "You are a helpful assistant.";
                if (assistantInstruction != null && !assistantInstruction.isEmpty()) {
                    systemPrompt = assistantInstruction;
                }

                // Add document context to system prompt if available
                if (documentContext != null && !documentContext.trim().isEmpty()) {
                    systemPrompt += "\n\nRelevant document context:\n" + documentContext;
                }

                inputParams.put("system_prompt", systemPrompt);
                inputParams.put("reasoning_effort", "medium");

            } else if (model.contains("black-forest-labs/flux-kontext-max")) {
                // flux-kontext-max uses input_image and optional output_format
                inputParams.clear();
                inputParams.put("prompt", input);
                if (!imageUrls.isEmpty()) {
                    inputParams.put("input_image", imageUrls.get(0));
                }
                inputParams.put("output_format", "jpg");
            } else if (model.contains("qwen/qwen-image-edit")) {
                // qwen image edit uses image and prompt; optional output_quality
                inputParams.clear();
                inputParams.put("prompt", input);
                if (!imageUrls.isEmpty()) {
                    inputParams.put("image", imageUrls.get(0));
                }
                inputParams.put("output_quality", 80);
            } else {
                // Default handling for other models
                String systemPrompt = "You are a helpful assistant.";
                if (assistantInstruction != null && !assistantInstruction.isEmpty()) {
                    systemPrompt = assistantInstruction;
                }

                // Add document context to system prompt if available
                if (documentContext != null && !documentContext.trim().isEmpty()) {
                    systemPrompt += "\n\nRelevant document context:\n" + documentContext;
                }

                inputParams.put("system_prompt", systemPrompt);
                inputParams.put("max_tokens", 4096);
                inputParams.put("temperature", 0.7);
            }

            System.out.println("Using model-specific parameters for: " + model);
            System.out.println("Assistant instruction being used: " + assistantInstruction);
            System.out.println("Payload input params: " + objectMapper.writeValueAsString(inputParams));

            requestBody.put("input", inputParams);

            System.out.println("Request body: " + objectMapper.writeValueAsString(requestBody));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            System.out.println("Replicate API response: " + response.getBody());

            // Parse response to get prediction ID
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String predictionId = responseJson.get("id").asText();

            System.out.println("Prediction ID: " + predictionId);

            // Step 2: Poll for completion
            return pollForCompletion(predictionId);

        } catch (Exception e) {
            System.err.println("Error in Replicate API call: " + e.getMessage());
            e.printStackTrace();
            return new AiChatResponse("assistant",
                    "Sorry, I encountered an error with the AI service. Please try again.");
        }
    }

    private String getModelVersion(String model) {
        // Map frontend model names to actual Replicate model versions
        Map<String, String> modelVersions = new HashMap<>();

        // Using the actual model identifiers from Replicate
        modelVersions.put("openai/o4-mini", "openai/o4-mini");
        modelVersions.put("openai/gpt-5", "openai/gpt-5");
        modelVersions.put("anthropic/claude-4-sonnet", "anthropic/claude-4-sonnet");
        modelVersions.put("anthropic/claude-3.7-sonnet", "anthropic/claude-3.7-sonnet");
        modelVersions.put("deepseek-ai/deepseek-v3", "deepseek-ai/deepseek-v3");
        modelVersions.put("black-forest-labs/flux-kontext-max", "black-forest-labs/flux-kontext-max");
        modelVersions.put("qwen/qwen-image-edit", "qwen/qwen-image-edit");

        // Default to OpenAI o4-mini for any other non-Gemini models
        return modelVersions.getOrDefault(model, "openai/o4-mini");
    }

    private List<String> extractImageUrls(String text) {
        // Extract image URLs from text - look for URLs that end with image extensions
        if (text == null)
            return List.of();

        // More comprehensive URL extraction using regex
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "https?://[^\\s]+\\.(jpg|jpeg|png|gif|webp|bmp|tiff|svg)(\\?[^\\s]*)?",
                java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(text);

        java.util.ArrayList<String> urls = new java.util.ArrayList<>();
        while (matcher.find()) {
            String url = matcher.group();
            // Clean up any trailing punctuation
            url = url.replaceAll("[\\),.]+$", "");
            urls.add(url);
        }

        System.out.println("[AI] Extracted " + urls.size() + " image URLs from text: " + text);
        System.out.println("[AI] URLs found: " + urls);

        return urls;
    }

    private AiChatResponse pollForCompletion(String predictionId) {
        try {
            String url = "https://api.replicate.com/v1/predictions/" + predictionId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + replicateApiKey);

            int maxAttempts = 60; // Maximum 60 attempts (120 seconds with 2-second intervals)
            int attempts = 0;

            System.out.println("Starting to poll for prediction completion: " + predictionId);

            while (attempts < maxAttempts) {
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

                JsonNode responseJson = objectMapper.readTree(response.getBody());
                String status = responseJson.get("status").asText();

                System.out.println("Attempt " + (attempts + 1) + ", Status: " + status);

                if ("succeeded".equals(status)) {
                    JsonNode output = responseJson.get("output");
                    System.out.println("Output received: " + output);

                    if (output != null && output.isArray() && output.size() > 0) {
                        // Check if this is a text response (array of tokens) or image URLs
                        boolean isTextResponse = true;
                        StringBuilder fullResponse = new StringBuilder();

                        for (JsonNode node : output) {
                            String val = node.asText();

                            // Check if this looks like an image URL
                            if (val.startsWith("http") && (val.contains(".jpg") || val.contains(".png")
                                    || val.contains(".gif") || val.contains(".webp"))) {
                                isTextResponse = false;
                                if (fullResponse.length() > 0)
                                    fullResponse.append("\n");
                                fullResponse.append(val);
                            } else {
                                // For text tokens, concatenate without newlines
                                fullResponse.append(val);
                            }
                        }

                        String generatedText = fullResponse.toString();
                        System.out.println("Generated output: " + generatedText);
                        return new AiChatResponse("assistant", generatedText);
                    } else if (output != null && output.isTextual()) {
                        String generatedText = output.asText();
                        System.out.println("Generated text: " + generatedText);
                        return new AiChatResponse("assistant", generatedText);
                    } else {
                        System.out.println("Output format unexpected: " + output);
                        return new AiChatResponse("assistant", "Received response but could not parse the output.");
                    }
                } else if ("failed".equals(status) || "canceled".equals(status)) {
                    JsonNode error = responseJson.get("error");
                    String errorMsg = error != null ? error.asText() : "Unknown error";
                    System.err.println("Prediction failed with error: " + errorMsg);
                    return new AiChatResponse("assistant",
                            "Sorry, the AI request failed: " + errorMsg);
                }

                // Wait 2 seconds before next attempt for longer responses
                Thread.sleep(2000);
                attempts++;
            }

            return new AiChatResponse("assistant",
                    "Sorry, the request took too long to process. Please try again.");

        } catch (Exception e) {
            e.printStackTrace();
            return new AiChatResponse("assistant",
                    "Sorry, I encountered an error while processing your request. Please try again.");
        }
    }

    private AiChatResponse generateGeminiResponse(String input, String previousResponse) {
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            return new AiChatResponse("assistant",
                    "Gemini AI service is not configured properly. Please check the server configuration.");
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key="
                    + geminiApiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            // Build request body for Gemini API
            Map<String, Object> requestBody = new HashMap<>();

            // Build chat history if there's a previous AI response
            if (previousResponse != null && !previousResponse.equals("Loading...") && !previousResponse.isEmpty()) {
                Map<String, Object> userContent = new HashMap<>();
                userContent.put("role", "user");
                userContent.put("parts", List.of(Map.of("text", "Previous context")));

                Map<String, Object> modelContent = new HashMap<>();
                modelContent.put("role", "model");
                modelContent.put("parts", List.of(Map.of("text", previousResponse)));

                requestBody.put("contents", List.of(userContent, modelContent,
                        Map.of("role", "user", "parts", List.of(Map.of("text", input)))));
            } else {
                requestBody.put("contents", List.of(
                        Map.of("role", "user", "parts", List.of(Map.of("text", input)))));
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // Parse Gemini response
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            JsonNode candidates = responseJson.get("candidates");

            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null && content.get("parts") != null) {
                    JsonNode parts = content.get("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        String generatedText = parts.get(0).get("text").asText();
                        return new AiChatResponse("assistant", generatedText);
                    }
                }
            }

            return new AiChatResponse("assistant",
                    "Sorry, I could not generate a response. Please try again.");

        } catch (Exception e) {
            e.printStackTrace();
            return new AiChatResponse("assistant",
                    "Sorry, I encountered an error with Gemini service. Please try again.");
        }
    }
}
