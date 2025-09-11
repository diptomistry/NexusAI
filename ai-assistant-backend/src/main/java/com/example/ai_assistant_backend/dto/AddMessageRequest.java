package com.example.ai_assistant_backend.dto;

public class AddMessageRequest {
    private Long conversationId;
    private String role;
    private String content;

    // Constructors
    public AddMessageRequest() {
    }

    public AddMessageRequest(Long conversationId, String role, String content) {
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
    }

    // Getters and setters
    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
