package com.factory.factorypattern.service;

import com.factory.factorypattern.model.PayoutRequest;
import com.factory.factorypattern.model.PayoutResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GCash processor
 * File Location: src/test/java/com/remittance/service/GCashProcessorTest.java
 *
 * Tests specific business logic for Philippines mobile wallet
 */
class GCashProcessorTest {

    private GCashProcessor gCashProcessor;

    @BeforeEach
    void setUp() {
        gCashProcessor = new GCashProcessor();
    }

    @Test
    @DisplayName("🇵🇭 GCash processes valid request successfully")
    void shouldProcessValidGCashRequest() {
        // Given
        PayoutRequest request = createValidGCashRequest();

        // When
        PayoutResponse response = gCashProcessor.processTransfer(request);

        // Then
        assertEquals(PayoutResponse.Status.SUCCESS, response.getStatus());
        assertEquals("GCash Philippines", response.getProviderName());
        assertTrue(response.getTransactionId().startsWith("GC"));
        assertEquals(request.getAmount(), response.getAmount());
        assertEquals("Juan Dela Cruz", response.getRecipientName());
    }

    @Test
    @DisplayName("❌ GCash fails for missing phone number")
    void shouldFailForMissingPhoneNumber() {
        // Given
        PayoutRequest request = createValidGCashRequest();
        request.setRecipientPhone(null);

        // When
        PayoutResponse response = gCashProcessor.processTransfer(request);

        // Then
        assertEquals(PayoutResponse.Status.FAILED, response.getStatus());
        assertEquals("GCASH_VALIDATION_ERROR", response.getErrorCode());
        assertTrue(response.getMessage().contains("Invalid GCash request parameters"));
    }

    @Test
    @DisplayName("❌ GCash fails for invalid phone number format")
    void shouldFailForInvalidPhoneNumber() {
        // Given
        PayoutRequest request = createValidGCashRequest();
        request.setRecipientPhone("invalid_phone");

        // When
        PayoutResponse response = gCashProcessor.processTransfer(request);

        // Then
        assertEquals(PayoutResponse.Status.FAILED, response.getStatus());
        assertEquals("GCASH_VALIDATION_ERROR", response.getErrorCode());
    }

    @Test
    @DisplayName("✅ GCash accepts valid Philippines phone formats")
    void shouldAcceptValidPhilippinesPhoneFormats() {
        String[] validPhones = {
                "+639123456789",
                "639123456789",
                "09123456789"
        };

        for (String phone : validPhones) {
            PayoutRequest request = createValidGCashRequest();
            request.setRecipientPhone(phone);

            PayoutResponse response = gCashProcessor.processTransfer(request);

            assertEquals(PayoutResponse.Status.SUCCESS, response.getStatus(),
                    "Should accept phone: " + phone);
        }
    }

    @Test
    @DisplayName("❌ GCash fails for amount exceeding limit")
    void shouldFailForAmountExceedingLimit() {
        // Given
        PayoutRequest request = createValidGCashRequest();
        request.setAmount(new BigDecimal("60000")); // Exceeds 50000 limit

        // When
        PayoutResponse response = gCashProcessor.processTransfer(request);

        // Then
        assertEquals(PayoutResponse.Status.FAILED, response.getStatus());
        assertEquals("GCASH_VALIDATION_ERROR", response.getErrorCode());
    }

    @Test
    @DisplayName("✅ GCash accepts amounts within limit")
    void shouldAcceptAmountsWithinLimit() {
        BigDecimal[] validAmounts = {
                new BigDecimal("1"),
                new BigDecimal("1000"),
                new BigDecimal("25000"),
                new BigDecimal("50000") // Exactly at limit
        };

        for (BigDecimal amount : validAmounts) {
            PayoutRequest request = createValidGCashRequest();
            request.setAmount(amount);

            PayoutResponse response = gCashProcessor.processTransfer(request);

            assertEquals(PayoutResponse.Status.SUCCESS, response.getStatus(),
                    "Should accept amount: " + amount);
        }
    }

    @Test
    @DisplayName("✅ GCash supports Philippines and PH")
    void shouldSupportPhilippines() {
        assertTrue(gCashProcessor.isSupported("philippines", "mobile_wallet"));
        assertTrue(gCashProcessor.isSupported("ph", "mobile_wallet"));

        // Should not support other countries
        assertFalse(gCashProcessor.isSupported("india", "mobile_wallet"));
        assertFalse(gCashProcessor.isSupported("japan", "mobile_wallet"));

        // Should not support other methods
        assertFalse(gCashProcessor.isSupported("philippines", "bank_transfer"));
        assertFalse(gCashProcessor.isSupported("philippines", "cash_pickup"));
    }

    @Test
    @DisplayName("🔍 GCash returns correct provider information")
    void shouldReturnCorrectProviderInformation() {
        assertEquals("GCash Philippines", gCashProcessor.getProviderName());

        String[] countries = gCashProcessor.getSupportedCountries();
        assertTrue(countries.length >= 2);
        assertTrue(java.util.Arrays.asList(countries).contains("philippines"));
        assertTrue(java.util.Arrays.asList(countries).contains("ph"));

        String[] methods = gCashProcessor.getSupportedMethods();
        assertTrue(methods.length >= 1);
        assertTrue(java.util.Arrays.asList(methods).contains("mobile_wallet"));
    }

    @Test
    @DisplayName("🎯 GCash validates request using interface method")
    void shouldValidateRequestUsingInterfaceMethod() {
        // Valid request
        PayoutRequest validRequest = createValidGCashRequest();
        assertTrue(gCashProcessor.validateRequest(validRequest));

        // Invalid request - null
        assertFalse(gCashProcessor.validateRequest(null));

        // Invalid request - negative amount
        PayoutRequest invalidRequest = createValidGCashRequest();
        invalidRequest.setAmount(new BigDecimal("-100"));
        assertFalse(gCashProcessor.validateRequest(invalidRequest));

        // Invalid request - zero amount
        invalidRequest.setAmount(BigDecimal.ZERO);
        assertFalse(gCashProcessor.validateRequest(invalidRequest));
    }

    @Test
    @DisplayName("🆔 GCash generates unique transaction IDs")
    void shouldGenerateUniqueTransactionIds() {
        PayoutRequest request = createValidGCashRequest();

        // Process multiple requests
        PayoutResponse response1 = gCashProcessor.processTransfer(request);
        PayoutResponse response2 = gCashProcessor.processTransfer(request);
        PayoutResponse response3 = gCashProcessor.processTransfer(request);

        // All should be successful
        assertEquals(PayoutResponse.Status.SUCCESS, response1.getStatus());
        assertEquals(PayoutResponse.Status.SUCCESS, response2.getStatus());
        assertEquals(PayoutResponse.Status.SUCCESS, response3.getStatus());

        // Transaction IDs should be unique
        assertNotEquals(response1.getTransactionId(), response2.getTransactionId());
        assertNotEquals(response2.getTransactionId(), response3.getTransactionId());
        assertNotEquals(response1.getTransactionId(), response3.getTransactionId());

        // All should start with "GC"
        assertTrue(response1.getTransactionId().startsWith("GC"));
        assertTrue(response2.getTransactionId().startsWith("GC"));
        assertTrue(response3.getTransactionId().startsWith("GC"));
    }

    // Helper method to create valid GCash request
    private PayoutRequest createValidGCashRequest() {
        PayoutRequest request = new PayoutRequest();
        request.setPayoutMethod("mobile_wallet");
        request.setDestinationCountry("philippines");
        request.setAmount(new BigDecimal("1000"));
        request.setCurrency("PHP");
        request.setRecipientName("Juan Dela Cruz");
        request.setRecipientPhone("+639123456789");
        request.setPurpose("Family support");
        return request;
    }
}
