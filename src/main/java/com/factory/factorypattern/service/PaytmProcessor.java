package com.factory.factorypattern.service;

import com.factory.factorypattern.model.PayoutProcessor;
import com.factory.factorypattern.model.PayoutRequest;
import com.factory.factorypattern.model.PayoutResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 🎯 FACTORY PATTERN - Concrete Product 2
 *
 * Paytm processor for India mobile wallet transfers.
 * Another concrete implementation that demonstrates factory pattern flexibility.
 *
 * Factory Pattern Role:
 * - Different implementation but same interface as GCash
 * - Factory can create this without client code changes
 * - Encapsulates India/Paytm specific business logic
 */
@Component
public class PaytmProcessor implements PayoutProcessor {

    private static final String PROVIDER_NAME = "Paytm India";
    private static final String[] SUPPORTED_COUNTRIES = {"india", "in"};
    private static final String[] SUPPORTED_METHODS = {"mobile_wallet", "digital_wallet"};

    @Override
    public PayoutResponse processTransfer(PayoutRequest request) {
        System.out.println("🇮🇳 Processing Paytm transfer for: " + request.getRecipientName());

        try {
            // Validate Paytm specific requirements
            if (!validatePaytmRequest(request)) {
                return PayoutResponse.failed("Invalid Paytm request parameters",
                        PROVIDER_NAME, "PAYTM_VALIDATION_ERROR");
            }

            // Simulate Paytm API call
            String transactionId = generatePaytmTransactionId();

            // Simulate processing delay
            Thread.sleep(800);

            System.out.println("✅ Paytm transfer successful. TxnId: " + transactionId);

            PayoutResponse response = PayoutResponse.success(transactionId, PROVIDER_NAME, request.getAmount());
            response.setCurrency(request.getCurrency());
            response.setRecipientName(request.getRecipientName());

            return response;

        } catch (Exception e) {
            System.err.println("❌ Paytm transfer failed: " + e.getMessage());
            return PayoutResponse.failed("Paytm processing failed: " + e.getMessage(),
                    PROVIDER_NAME, "PAYTM_API_ERROR");
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
     * Paytm specific validation
     */
    private boolean validatePaytmRequest(PayoutRequest request) {
        // Check phone number or email for India
        String phone = request.getRecipientPhone();
        String email = request.getRecipientEmail();

        if ((phone == null || phone.trim().isEmpty()) &&
                (email == null || email.trim().isEmpty())) {
            System.err.println("❌ Paytm requires recipient phone or email");
            return false;
        }

        // Simple India phone validation if provided
        if (phone != null && !phone.trim().isEmpty()) {
            if (!phone.matches("^(\\+91|91|0)?[6-9]\\d{9}$")) {
                System.err.println("❌ Invalid India phone number format: " + phone);
                return false;
            }
        }

        // Check amount limits (Paytm specific)
        if (request.getAmount().compareTo(new java.math.BigDecimal("100000")) > 0) {
            System.err.println("❌ Amount exceeds Paytm daily limit");
            return false;
        }

        return true;
    }

    /**
     * Generate Paytm style transaction ID
     */
    private String generatePaytmTransactionId() {
        return "PTM" + System.currentTimeMillis() +
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
