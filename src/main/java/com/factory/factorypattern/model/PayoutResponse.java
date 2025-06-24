package com.factory.factorypattern.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response model for payout transfers
 * Standardized response from all processors
 */
public class PayoutResponse {

    public enum Status {
        SUCCESS, FAILED, PENDING, CANCELLED
    }

    private Status status;
    private String transactionId;
    private String message;
    private String providerName;
    private BigDecimal amount;
    private String currency;
    private String recipientName;
    private LocalDateTime processedAt;
    private String errorCode;
    private String errorDetails;

    // Constructors
    public PayoutResponse() {
        this.processedAt = LocalDateTime.now();
    }

    public PayoutResponse(Status status, String transactionId, String message, String providerName) {
        this();
        this.status = status;
        this.transactionId = transactionId;
        this.message = message;
        this.providerName = providerName;
    }

    // Static factory methods for common responses
    public static PayoutResponse success(String transactionId, String providerName, BigDecimal amount) {
        PayoutResponse response = new PayoutResponse(Status.SUCCESS, transactionId,
                "Transfer completed successfully", providerName);
        response.setAmount(amount);
        return response;
    }

    public static PayoutResponse failed(String message, String providerName, String errorCode) {
        PayoutResponse response = new PayoutResponse(Status.FAILED, null, message, providerName);
        response.setErrorCode(errorCode);
        return response;
    }

    // Getters and Setters
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorDetails() { return errorDetails; }
    public void setErrorDetails(String errorDetails) { this.errorDetails = errorDetails; }
}