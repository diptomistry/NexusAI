package com.example.ai_assistant_backend.dto;

public class AiChatResponse {
    private String role;
    private String content;

    // Constructors
    public AiChatResponse() {
    }

    public AiChatResponse(String role, String content) {
        this.role = role;
        this.content = content;
    }

    // Getters and Setters
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
