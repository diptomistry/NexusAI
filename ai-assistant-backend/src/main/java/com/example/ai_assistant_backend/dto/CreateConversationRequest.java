package com.example.ai_assistant_backend.dto;

public class CreateConversationRequest {
    private String userId;
    private String assistantId;
    private String title;

    // Constructors
    public CreateConversationRequest() {
    }

    public CreateConversationRequest(String userId, String assistantId, String title) {
        this.userId = userId;
        this.assistantId = assistantId;
        this.title = title;
    }

    // Getters and setters
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
