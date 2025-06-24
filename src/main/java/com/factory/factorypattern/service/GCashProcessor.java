package com.factory.factorypattern.service;

import com.factory.factorypattern.model.PayoutProcessor;
import com.factory.factorypattern.model.PayoutRequest;
import com.factory.factorypattern.model.PayoutResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 🎯 FACTORY PATTERN - Concrete Product 1
 *
 * GCash processor for Philippines mobile wallet transfers.
 * This is one of the concrete implementations that the factory can create.
 *
 * Factory Pattern Role:
 * - Concrete implementation of PayoutProcessor interface
 * - Encapsulates GCash-specific business logic
 * - Can be created by factory without client knowing implementation details
 */
@Component
public class GCashProcessor implements PayoutProcessor {

    private static final String PROVIDER_NAME = "GCash Philippines";
    private static final String[] SUPPORTED_COUNTRIES = {"philippines", "ph"};
    private static final String[] SUPPORTED_METHODS = {"mobile_wallet"};

    @Override
    public PayoutResponse processTransfer(PayoutRequest request) {
        System.out.println("🇵🇭 Processing GCash transfer for: " + request.getRecipientName());

        try {
            // Validate GCash specific requirements
            if (!validateGCashRequest(request)) {
                return PayoutResponse.failed("Invalid GCash request parameters",
                        PROVIDER_NAME, "GCASH_VALIDATION_ERROR");
            }

            // Simulate GCash API call
            String transactionId = generateGCashTransactionId();

            // Simulate processing delay
            Thread.sleep(1000);

            System.out.println("✅ GCash transfer successful. TxnId: " + transactionId);

            PayoutResponse response = PayoutResponse.success(transactionId, PROVIDER_NAME, request.getAmount());
            response.setCurrency(request.getCurrency());
            response.setRecipientName(request.getRecipientName());

            return response;

        } catch (Exception e) {
            System.err.println("❌ GCash transfer failed: " + e.getMessage());
            return PayoutResponse.failed("GCash processing failed: " + e.getMessage(),
                    PROVIDER_NAME, "GCASH_API_ERROR");
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
     * GCash specific validation
     */
    private boolean validateGCashRequest(PayoutRequest request) {
        // Check phone number format for Philippines
        String phone = request.getRecipientPhone();
        if (phone == null || phone.trim().isEmpty()) {
            System.err.println("❌ GCash requires recipient phone number");
            return false;
        }

        // Simple Philippines phone validation
        if (!phone.matches("^(\\+63|63|0)?9\\d{9}$")) {
            System.err.println("❌ Invalid Philippines phone number format: " + phone);
            return false;
        }

        // Check amount limits (GCash specific)
        if (request.getAmount().compareTo(new java.math.BigDecimal("50000")) > 0) {
            System.err.println("❌ Amount exceeds GCash daily limit");
            return false;
        }

        return true;
    }

    /**
     * Generate GCash style transaction ID
     */
    private String generateGCashTransactionId() {
        return "GC" + System.currentTimeMillis() +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
