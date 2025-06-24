package com.factory.factorypattern.service;

import com.factory.factorypattern.model.PayoutProcessor;
import com.factory.factorypattern.model.PayoutRequest;
import com.factory.factorypattern.model.PayoutResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 🎯 FACTORY PATTERN - Concrete Product 3
 *
 * Bank transfer processor for multiple countries.
 * Demonstrates how factory pattern handles generic implementations.
 *
 * Factory Pattern Role:
 * - Generic processor that works across multiple countries
 * - Shows factory flexibility in creating different types of processors
 * - Same interface but different business logic than wallet processors
 */
@Component
public class BankTransferProcessor implements PayoutProcessor {

    private static final String PROVIDER_NAME = "International Bank Transfer";
    private static final String[] SUPPORTED_COUNTRIES = {
            "india", "philippines", "bangladesh", "nepal", "sri lanka",
            "in", "ph", "bd", "np", "lk"
    };
    private static final String[] SUPPORTED_METHODS = {"bank_transfer", "wire_transfer"};

    @Override
    public PayoutResponse processTransfer(PayoutRequest request) {
        System.out.println("🏦 Processing Bank transfer for: " + request.getRecipientName() +
                " in " + request.getDestinationCountry());

        try {
            // Validate bank transfer specific requirements
            if (!validateBankTransferRequest(request)) {
                return PayoutResponse.failed("Invalid bank transfer request parameters",
                        PROVIDER_NAME, "BANK_VALIDATION_ERROR");
            }

            // Simulate bank API call
            String transactionId = generateBankTransactionId();

            // Bank transfers typically take longer
            Thread.sleep(1500);

            System.out.println("✅ Bank transfer initiated successfully. TxnId: " + transactionId);

            PayoutResponse response = PayoutResponse.success(transactionId, PROVIDER_NAME, request.getAmount());
            response.setCurrency(request.getCurrency());
            response.setRecipientName(request.getRecipientName());
            response.setMessage("Bank transfer initiated successfully. Processing time: 1-3 business days");

            return response;

        } catch (Exception e) {
            System.err.println("❌ Bank transfer failed: " + e.getMessage());
            return PayoutResponse.failed("Bank transfer processing failed: " + e.getMessage(),
                    PROVIDER_NAME, "BANK_API_ERROR");
        }
    }

    @Override
    public boolean isSupported(String country, String method) {
        boolean countrySupported = country != null &&
                java.util.Arrays.stream(SUPPORTED_COUNTRIES)
                        .anyMatch(c -> c.equalsIgnoreCase(country.trim()));
        boolean methodSupported = method != null &&
                java.util.Arrays.stream(SUPPORTED_METHODS)
                        .anyMatch(m -> m.equalsIgnoreCase(method.trim()));
        return countrySupported && methodSupported;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public String[] getSupportedCountries() {
        return SUPPORTED_COUNTRIES.clone();
    }

    @Override
    public String[] getSupportedMethods() {
        return SUPPORTED_METHODS.clone();
    }

    /**
     * Bank transfer specific validation
     */
    private boolean validateBankTransferRequest(PayoutRequest request) {
        // Check bank account details
        String bankAccount = request.getBankAccount();
        if (bankAccount == null || bankAccount.trim().isEmpty()) {
            System.err.println("❌ Bank transfer requires recipient bank account");
            return false;
        }

        String bankCode = request.getBankCode();
        if (bankCode == null || bankCode.trim().isEmpty()) {
            System.err.println("❌ Bank transfer requires bank code/SWIFT code");
            return false;
        }

        // Check minimum amount for bank transfers
        if (request.getAmount().compareTo(new java.math.BigDecimal("10")) < 0) {
            System.err.println("❌ Minimum amount for bank transfer is $10");
            return false;
        }

        // Check maximum amount
        if (request.getAmount().compareTo(new java.math.BigDecimal("500000")) > 0) {
            System.err.println("❌ Amount exceeds bank transfer daily limit");
            return false;
        }

        return true;
    }

    /**
     * Generate bank style transaction ID
     */
    private String generateBankTransactionId() {
        return "BT" + System.currentTimeMillis() +
                UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }
}