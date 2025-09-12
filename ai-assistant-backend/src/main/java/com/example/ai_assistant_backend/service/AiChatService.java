package com.example.ai_assistant_backend.service;

import com.example.ai_assistant_backend.dto.AiChatRequest;
import com.example.ai_assistant_backend.dto.AiChatResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@Service
public class AiChatService {

    @Value("${replicate.api.key:}")
    private String replicateApiKey;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AiChatService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public AiChatResponse generateResponse(AiChatRequest request) {
        try {
            String provider = request.getProvider();
            String userInput = request.getUserInput();
            String assistantInstruction = request.getAssistantInstruction();

            System.out.println("Received request for provider: " + provider);
            System.out.println("User input: " + userInput);
            System.out.println("Assistant instruction: " + assistantInstruction);

            // Check if it's a Gemini model
            if (provider != null && (provider.contains("google") || provider.contains("gemini"))) {
                String fullInput = userInput;
                if (assistantInstruction != null && !assistantInstruction.isEmpty()) {
                    fullInput = userInput + ":-" + assistantInstruction;
                }
                return generateGeminiResponse(fullInput, request.getAiResp());
            } else {
                // Use Replicate for all other models (OpenAI, Mistral, Anthropic, etc.)
                return generateReplicateResponse(provider, userInput, assistantInstruction);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new AiChatResponse("assistant",
                    "Sorry, I encountered an error processing your request. Please try again.");
        }
    }

    private AiChatResponse generateReplicateResponse(String model, String input, String assistantInstruction) {
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
            inputParams.put("prompt", input);

            // Model-specific parameter handling
            if (model.contains("deepseek")) {
                // DeepSeek models might use different parameters
                String fullPrompt = input;
                if (assistantInstruction != null && !assistantInstruction.isEmpty()) {
                    fullPrompt = assistantInstruction + "\n\nUser: " + input + "\nAssistant:";
                }
                inputParams.put("prompt", fullPrompt);
                inputParams.put("max_tokens", 4096);
                inputParams.put("temperature", 0.7);

            } else if (model.contains("anthropic")) {
                // Anthropic models (Claude)
                String systemPrompt = "You are a helpful assistant.";
                if (assistantInstruction != null && !assistantInstruction.isEmpty()) {
                    systemPrompt = assistantInstruction;
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
                inputParams.put("system_prompt", systemPrompt);
                inputParams.put("reasoning_effort", "medium");

            } else {
                // Default handling for other models
                String systemPrompt = "You are a helpful assistant.";
                if (assistantInstruction != null && !assistantInstruction.isEmpty()) {
                    systemPrompt = assistantInstruction;
                }
                inputParams.put("system_prompt", systemPrompt);
                inputParams.put("max_tokens", 4096);
                inputParams.put("temperature", 0.7);
            }

            System.out.println("Using model-specific parameters for: " + model);
            System.out.println("Assistant instruction being used: " + assistantInstruction);

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

        // Default to OpenAI o4-mini for any other non-Gemini models
        return modelVersions.getOrDefault(model, "openai/o4-mini");
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
                        // Join all array elements to get complete response
                        StringBuilder fullResponse = new StringBuilder();
                        for (JsonNode node : output) {
                            fullResponse.append(node.asText());
                        }
                        String generatedText = fullResponse.toString();
                        System.out.println("Generated text: " + generatedText);
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
