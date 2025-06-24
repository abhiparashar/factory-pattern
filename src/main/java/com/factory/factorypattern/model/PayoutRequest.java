package com.factory.factorypattern.model;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Request model for payout transfers
 * Contains all necessary data for factory decision making
 */
public class PayoutRequest {

    @NotBlank(message = "Payout method is required")
    private String payoutMethod;

    @NotBlank(message = "Destination country is required")
    private String destinationCountry;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotBlank(message = "Recipient name is required")
    private String recipientName;

    private String recipientPhone;
    private String recipientEmail;
    private String bankAccount;
    private String bankCode;
    private String purpose;

    // Constructors
    public PayoutRequest() {}

    public PayoutRequest(String payoutMethod, String destinationCountry,
                         BigDecimal amount, String currency, String recipientName) {
        this.payoutMethod = payoutMethod;
        this.destinationCountry = destinationCountry;
        this.amount = amount;
        this.currency = currency;
        this.recipientName = recipientName;
    }

    // Getters and Setters
    public String getPayoutMethod() { return payoutMethod; }
    public void setPayoutMethod(String payoutMethod) { this.payoutMethod = payoutMethod; }

    public String getDestinationCountry() { return destinationCountry; }
    public void setDestinationCountry(String destinationCountry) { this.destinationCountry = destinationCountry; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }

    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    @Override
    public String toString() {
        return "PayoutRequest{" +
                "payoutMethod='" + payoutMethod + '\'' +
                ", destinationCountry='" + destinationCountry + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", recipientName='" + recipientName + '\'' +
                '}';
    }
}