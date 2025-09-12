package com.example.ai_assistant_backend.dto;

public class PaymentResponse {
    private boolean success;
    private String message;
    private String transactionId;
    private String gatewayPageUrl;
    private Object data;

    // Constructors
    public PaymentResponse() {
    }

    public PaymentResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public PaymentResponse(boolean success, String message, String transactionId, String gatewayPageUrl) {
        this.success = success;
        this.message = message;
        this.transactionId = transactionId;
        this.gatewayPageUrl = gatewayPageUrl;
    }

    // Static factory methods
    public static PaymentResponse success(String message, String transactionId, String gatewayPageUrl) {
        return new PaymentResponse(true, message, transactionId, gatewayPageUrl);
    }

    public static PaymentResponse failure(String message) {
        return new PaymentResponse(false, message);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getGatewayPageUrl() {
        return gatewayPageUrl;
    }

    public void setGatewayPageUrl(String gatewayPageUrl) {
        this.gatewayPageUrl = gatewayPageUrl;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
