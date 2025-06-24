package com.factory.factorypattern.service;

import com.factory.factorypattern.model.PayoutRequest;
import com.factory.factorypattern.model.PayoutResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Bank Transfer processor
 * File Location: src/test/java/com/factory/factorypattern/service/BankTransferProcessorTest.java
 *
 * Tests specific business logic for international bank transfers
 * This processor handles multiple countries and validates bank-specific requirements
 */
@Service
class BankTransferProcessorTest {

    private BankTransferProcessor bankTransferProcessor;

    @BeforeEach
    void setUp() {
        bankTransferProcessor = new BankTransferProcessor();
    }

    // 🏦 SUCCESSFUL PROCESSING TESTS

    @Test
    @DisplayName("🏦 Bank Transfer processes valid request successfully")
    void shouldProcessValidBankTransferRequest() {
        // Given
        PayoutRequest request = createValidBankTransferRequest();

        // When
        PayoutResponse response = bankTransferProcessor.processTransfer(request);

        // Then
        assertEquals(PayoutResponse.Status.SUCCESS, response.getStatus());
        assertEquals("International Bank Transfer", response.getProviderName());
        assertTrue(response.getTransactionId().startsWith("BT"));
        assertEquals(request.getAmount(), response.getAmount());
        assertEquals("Maria Santos", response.getRecipientName());
        assertTrue(response.getMessage().contains("1-3 business days"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"philippines", "india", "bangladesh", "nepal", "sri lanka"})
    @DisplayName("🌍 Bank Transfer processes requests for multiple countries")
    void shouldProcessRequestsForMultipleCountries(String country) {
        // Given
        PayoutRequest request = createValidBankTransferRequest();
        request.setDestinationCountry(country);

        // When
        PayoutResponse response = bankTransferProcessor.processTransfer(request);

        // Then
        assertEquals(PayoutResponse.Status.SUCCESS, response.getStatus());
        assertEquals("International Bank Transfer", response.getProviderName());
        assertTrue(response.getTransactionId().startsWith("BT"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"bank_transfer", "wire_transfer"})
    @DisplayName("💳 Bank Transfer supports both bank_transfer and wire_transfer methods")
    void shouldSupportMultipleTransferMethods(String method) {
        // Given
        PayoutRequest request = createValidBankTransferRequest();
        request.setPayoutMethod(method);

        // When
        PayoutResponse response = bankTransferProcessor.processTransfer(request);

        // Then
        assertEquals(PayoutResponse.Status.SUCCESS, response.getStatus());
        assertEquals("International Bank Transfer", response.getProviderName());
    }

    // 🔍 VALIDATION TESTS

    @Test
    @DisplayName("❌ Bank Transfer fails for missing bank account")
    void shouldFailForMissingBankAccount() {
        // Given
        PayoutRequest request = createValidBankTransferRequest();
        request.setBankAccount(null);

        // When
        PayoutResponse response = bankTransferProcessor.processTransfer(request);

        // Then
        assertEquals(PayoutResponse.Status.FAILED, response.getStatus());
        assertEquals("BANK_VALIDATION_ERROR", response.getErrorCode());
        assertTrue(response.getMessage().contains("Invalid bank transfer request parameters"));
    }

    @Test
    @DisplayName("❌ Bank Transfer fails for empty bank account")
    void shouldFailForEmptyBankAccount() {
        // Given
        PayoutRequest request = createValidBankTransferRequest();
        request.setBankAccount("   ");  // Empty/whitespace

        // When
        PayoutResponse response = bankTransferProcessor.processTransfer(request);

        // Then
        assertEquals(PayoutResponse.Status.FAILED, response.getStatus());
        assertEquals("BANK_VALIDATION_ERROR", response.getErrorCode());
    }

    @Test
    @DisplayName("❌ Bank Transfer fails for missing bank code")
    void shouldFailForMissingBankCode() {
        // Given
        PayoutRequest request = createValidBankTransferRequest();
        request.setBankCode(null);

        // When
        PayoutResponse response = bankTransferProcessor.processTransfer(request);

        // Then
        assertEquals(PayoutResponse.Status.FAILED, response.getStatus());
        assertEquals("BANK_VALIDATION_ERROR", response.getErrorCode());
    }

    @Test
    @DisplayName("❌ Bank Transfer fails for empty bank code")
    void shouldFailForEmptyBankCode() {
        // Given
        PayoutRequest request = createValidBankTransferRequest();
        request.setBankCode("");

        // When
        PayoutResponse response = bankTransferProcessor.processTransfer(request);

        // Then
        assertEquals(PayoutResponse.Status.FAILED, response.getStatus());
        assertEquals("BANK_VALIDATION_ERROR", response.getErrorCode());
    }

    // 💰 AMOUNT VALIDATION TESTS

    @Test
    @DisplayName("❌ Bank Transfer fails for amount below minimum")
    void shouldFailForAmountBelowMinimum() {
        // Given
        PayoutRequest request = createValidBankTransferRequest();
        request.setAmount(new BigDecimal("5.00")); // Below $10 minimum

        // When
        PayoutResponse response = bankTransferProcessor.processTransfer(request);

        // Then
        assertEquals(PayoutResponse.Status.FAILED, response.getStatus());
        assertEquals("BANK_VALIDATION_ERROR", response.getErrorCode());
    }

    @Test
    @DisplayName("✅ Bank Transfer accepts minimum amount")
    void shouldAcceptMinimumAmount() {
        // Given
        PayoutRequest request = createValidBankTransferRequest();
        request.setAmount(new BigDecimal("10.00")); // Exactly $10 minimum

        // When
        PayoutResponse response = bankTransferProcessor.processTransfer(request);

        // Then
        assertEquals(PayoutResponse.Status.SUCCESS, response.getStatus());
        assertEquals(new BigDecimal("10.00"), response.getAmount());
    }

    @Test
    @DisplayName("❌ Bank Transfer fails for amount exceeding maximum")
    void shouldFailForAmountExceedingMaximum() {
        // Given
        PayoutRequest request = createValidBankTransferRequest();
        request.setAmount(new BigDecimal("600000")); // Exceeds $500,000 limit

        // When
        PayoutResponse response = bankTransferProcessor.processTransfer(request);

        // Then
        assertEquals(PayoutResponse.Status.FAILED, response.getStatus());
        assertEquals("BANK_VALIDATION_ERROR", response.getErrorCode());
    }

    @Test
    @DisplayName("✅ Bank Transfer accepts maximum amount")
    void shouldAcceptMaximumAmount() {
        // Given
        PayoutRequest request = createValidBankTransferRequest();
        request.setAmount(new BigDecimal("500000")); // Exactly $500,000 limit

        // When
        PayoutResponse response = bankTransferProcessor.processTransfer(request);

        // Then
        assertEquals(PayoutResponse.Status.SUCCESS, response.getStatus());
        assertEquals(new BigDecimal("500000"), response.getAmount());
    }

    @ParameterizedTest
    @ValueSource(strings = {"100", "1000", "50000", "250000", "499999"})
    @DisplayName("✅ Bank Transfer accepts amounts within valid range")
    void shouldAcceptAmountsWithinValidRange(String amountStr) {
        // Given
        PayoutRequest request = createValidBankTransferRequest();
        request.setAmount(new BigDecimal(amountStr));

        // When
        PayoutResponse response = bankTransferProcessor.processTransfer(request);

        // Then
        assertEquals(PayoutResponse.Status.SUCCESS, response.getStatus());
        assertEquals(new BigDecimal(amountStr), response.getAmount());
    }

    // 🌍 COUNTRY SUPPORT TESTS

    @Test
    @DisplayName("✅ Bank Transfer supports Philippines and country codes")
    void shouldSupportPhilippinesAndCountryCodes() {
        // Full country names
        assertTrue(bankTransferProcessor.isSupported("philippines", "bank_transfer"));
        assertTrue(bankTransferProcessor.isSupported("india", "bank_transfer"));
        assertTrue(bankTransferProcessor.isSupported("bangladesh", "bank_transfer"));
        assertTrue(bankTransferProcessor.isSupported("nepal", "bank_transfer"));
        assertTrue(bankTransferProcessor.isSupported("sri lanka", "bank_transfer"));

        // Country codes
        assertTrue(bankTransferProcessor.isSupported("ph", "bank_transfer"));
        assertTrue(bankTransferProcessor.isSupported("in", "bank_transfer"));
        assertTrue(bankTransferProcessor.isSupported("bd", "bank_transfer"));
        assertTrue(bankTransferProcessor.isSupported("np", "bank_transfer"));
        assertTrue(bankTransferProcessor.isSupported("lk", "bank_transfer"));
    }

    @Test
    @DisplayName("✅ Bank Transfer supports both bank_transfer and wire_transfer methods")
    void shouldSupportBothTransferMethods() {
        assertTrue(bankTransferProcessor.isSupported("philippines", "bank_transfer"));
        assertTrue(bankTransferProcessor.isSupported("philippines", "wire_transfer"));
        assertTrue(bankTransferProcessor.isSupported("india", "bank_transfer"));
        assertTrue(bankTransferProcessor.isSupported("india", "wire_transfer"));
    }

    @Test
    @DisplayName("❌ Bank Transfer does not support unsupported countries")
    void shouldNotSupportUnsupportedCountries() {
        assertFalse(bankTransferProcessor.isSupported("japan", "bank_transfer"));
        assertFalse(bankTransferProcessor.isSupported("usa", "bank_transfer"));
        assertFalse(bankTransferProcessor.isSupported("germany", "bank_transfer"));
        assertFalse(bankTransferProcessor.isSupported("mars", "bank_transfer"));
    }

    @Test
    @DisplayName("❌ Bank Transfer does not support mobile wallet methods")
    void shouldNotSupportMobileWalletMethods() {
        assertFalse(bankTransferProcessor.isSupported("philippines", "mobile_wallet"));
        assertFalse(bankTransferProcessor.isSupported("india", "digital_wallet"));
        assertFalse(bankTransferProcessor.isSupported("philippines", "cash_pickup"));
    }

    // 🔍 PROVIDER INFO TESTS

    @Test
    @DisplayName("🔍 Bank Transfer returns correct provider information")
    void shouldReturnCorrectProviderInformation() {
        assertEquals("International Bank Transfer", bankTransferProcessor.getProviderName());

        String[] countries = bankTransferProcessor.getSupportedCountries();
        assertTrue(countries.length >= 10); // Should have at least 10 countries/codes

        // Check some key countries are included
        assertTrue(java.util.Arrays.asList(countries).contains("philippines"));
        assertTrue(java.util.Arrays.asList(countries).contains("india"));
        assertTrue(java.util.Arrays.asList(countries).contains("ph"));
        assertTrue(java.util.Arrays.asList(countries).contains("in"));

        String[] methods = bankTransferProcessor.getSupportedMethods();
        assertTrue(methods.length >= 2);
        assertTrue(java.util.Arrays.asList(methods).contains("bank_transfer"));
        assertTrue(java.util.Arrays.asList(methods).contains("wire_transfer"));
    }

    // 🎯 INTERFACE VALIDATION TESTS

    @Test
    @DisplayName("🎯 Bank Transfer validates request using interface method")
    void shouldValidateRequestUsingInterfaceMethod() {
        // Valid request
        PayoutRequest validRequest = createValidBankTransferRequest();
        assertTrue(bankTransferProcessor.validateRequest(validRequest));

        // Invalid request - null
        assertFalse(bankTransferProcessor.validateRequest(null));

        // Invalid request - negative amount
        PayoutRequest invalidRequest = createValidBankTransferRequest();
        invalidRequest.setAmount(new BigDecimal("-100"));
        assertFalse(bankTransferProcessor.validateRequest(invalidRequest));

        // Invalid request - zero amount
        invalidRequest.setAmount(BigDecimal.ZERO);
        assertFalse(bankTransferProcessor.validateRequest(invalidRequest));

        // Invalid request - null amount
        invalidRequest.setAmount(null);
        assertFalse(bankTransferProcessor.validateRequest(invalidRequest));
    }

    // 🆔 TRANSACTION ID TESTS

    @Test
    @DisplayName("🆔 Bank Transfer generates unique transaction IDs")
    void shouldGenerateUniqueTransactionIds() {
        PayoutRequest request = createValidBankTransferRequest();

        // Process multiple requests
        PayoutResponse response1 = bankTransferProcessor.processTransfer(request);
        PayoutResponse response2 = bankTransferProcessor.processTransfer(request);
        PayoutResponse response3 = bankTransferProcessor.processTransfer(request);

        // All should be successful
        assertEquals(PayoutResponse.Status.SUCCESS, response1.getStatus());
        assertEquals(PayoutResponse.Status.SUCCESS, response2.getStatus());
        assertEquals(PayoutResponse.Status.SUCCESS, response3.getStatus());

        // Transaction IDs should be unique
        assertNotEquals(response1.getTransactionId(), response2.getTransactionId());
        assertNotEquals(response2.getTransactionId(), response3.getTransactionId());
        assertNotEquals(response1.getTransactionId(), response3.getTransactionId());

        // All should start with "BT"
        assertTrue(response1.getTransactionId().startsWith("BT"));
        assertTrue(response2.getTransactionId().startsWith("BT"));
        assertTrue(response3.getTransactionId().startsWith("BT"));

        // All should be longer than just "BT"
        assertTrue(response1.getTransactionId().length() > 10);
        assertTrue(response2.getTransactionId().length() > 10);
        assertTrue(response3.getTransactionId().length() > 10);
    }

    // 🕒 PROCESSING TIME TESTS

    @Test
    @DisplayName("🕒 Bank Transfer processing takes appropriate time")
    void shouldTakeAppropriateProcessingTime() {
        // Given
        PayoutRequest request = createValidBankTransferRequest();

        // When
        long startTime = System.currentTimeMillis();
        PayoutResponse response = bankTransferProcessor.processTransfer(request);
        long endTime = System.currentTimeMillis();

        // Then
        assertEquals(PayoutResponse.Status.SUCCESS, response.getStatus());

        // Should take at least 1 second (1000ms) due to Thread.sleep(1500)
        long processingTime = endTime - startTime;
        assertTrue(processingTime >= 1000, "Processing should take at least 1 second, took: " + processingTime + "ms");

        // Should not take more than 3 seconds (reasonable upper bound)
        assertTrue(processingTime < 3000, "Processing should not take more than 3 seconds, took: " + processingTime + "ms");
    }

    // 🔧 HELPER METHODS

    /**
     * Creates a valid bank transfer request for testing
     */
    private PayoutRequest createValidBankTransferRequest() {
        PayoutRequest request = new PayoutRequest();
        request.setPayoutMethod("bank_transfer");
        request.setDestinationCountry("philippines");
        request.setAmount(new BigDecimal("10000"));
        request.setCurrency("PHP");
        request.setRecipientName("Maria Santos");
        request.setBankAccount("1234567890123456");
        request.setBankCode("BDO001");
        request.setPurpose("Family support");
        return request;
    }

    /**
     * Creates a bank transfer request for India
     */
    private PayoutRequest createIndiaBankTransferRequest() {
        PayoutRequest request = new PayoutRequest();
        request.setPayoutMethod("bank_transfer");
        request.setDestinationCountry("india");
        request.setAmount(new BigDecimal("25000"));
        request.setCurrency("INR");
        request.setRecipientName("Raj Kumar");
        request.setBankAccount("9876543210");
        request.setBankCode("ICICI001");
        request.setPurpose("Business payment");
        return request;
    }

    /**
     * Creates a wire transfer request
     */
    private PayoutRequest createWireTransferRequest() {
        PayoutRequest request = new PayoutRequest();
        request.setPayoutMethod("wire_transfer");
        request.setDestinationCountry("bangladesh");
        request.setAmount(new BigDecimal("15000"));
        request.setCurrency("BDT");
        request.setRecipientName("Ahmed Hassan");
        request.setBankAccount("BD1234567890");
        request.setBankCode("SWIFT123");
        request.setPurpose("Remittance");
        return request;
    }
}
