package com.example.ai_assistant_backend.dto;

public class AiChatRequest {
    private String provider;
    private String userInput;
    private String aiResp;
    private String assistantInstruction;
    private String documentContext;
    private String userId;
    private String assistantId;

    // Constructors
    public AiChatRequest() {
    }

    public AiChatRequest(String provider, String userInput, String aiResp, String assistantInstruction) {
        this.provider = provider;
        this.userInput = userInput;
        this.aiResp = aiResp;
        this.assistantInstruction = assistantInstruction;
    }

    public AiChatRequest(String provider, String userInput, String aiResp, String assistantInstruction,
            String documentContext, String userId, String assistantId) {
        this.provider = provider;
        this.userInput = userInput;
        this.aiResp = aiResp;
        this.assistantInstruction = assistantInstruction;
        this.documentContext = documentContext;
        this.userId = userId;
        this.assistantId = assistantId;
    }

    // Getters and Setters
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }

    public String getAiResp() {
        return aiResp;
    }

    public void setAiResp(String aiResp) {
        this.aiResp = aiResp;
    }

    public String getAssistantInstruction() {
        return assistantInstruction;
    }

    public void setAssistantInstruction(String assistantInstruction) {
        this.assistantInstruction = assistantInstruction;
    }

    public String getDocumentContext() {
        return documentContext;
    }

    public void setDocumentContext(String documentContext) {
        this.documentContext = documentContext;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAssistantId() {
        return assistantId;
    }

    public void setAssistantId(String assistantId) {
        this.assistantId = assistantId;
    }
}
