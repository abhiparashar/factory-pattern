package com.factory.factorypattern.controller;

import com.factory.factorypattern.factory.PayoutMethodFactory;
import com.factory.factorypattern.service.BankTransferProcessor;
import com.factory.factorypattern.service.GCashProcessor;
import com.factory.factorypattern.service.PaytmProcessor;
import com.factory.factorypattern.model.PayoutProcessor;
import com.factory.factorypattern.model.PayoutRequest;
import com.factory.factorypattern.model.PayoutResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 🎯 MODERN FACTORY PATTERN INTEGRATION TESTS (Spring Boot 3.4+)
 *
 * This version uses the latest Spring Boot 3.4+ approach:
 * - Uses @MockitoBean (replaces deprecated @MockBean)
 * - Uses @TestConfiguration for explicit bean management
 * - Follows Spring Boot 3.4+ best practices
 * - Provides clean test isolation
 */
@WebMvcTest(TransferController.class)
class TransferControllerModernTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PayoutMethodFactory payoutMethodFactory;

    @Autowired
    private ObjectMapper objectMapper;

    private PayoutProcessor mockProcessor;

    /**
     * 🔧 SPRING BOOT 3.4+ APPROACH: Test Configuration
     * Provides required beans for the test context
     */
    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public GCashProcessor gCashProcessor() {
            return new GCashProcessor();
        }

        @Bean
        @Primary
        public PaytmProcessor paytmProcessor() {
            return new PaytmProcessor();
        }

        @Bean
        @Primary
        public BankTransferProcessor bankTransferProcessor() {
            return new BankTransferProcessor();
        }
    }

    @BeforeEach
    void setUp() {
        // Create a mock processor for testing
        mockProcessor = org.mockito.Mockito.mock(PayoutProcessor.class);
    }

    @Test
    @DisplayName("🎯 Controller uses factory to process transfer successfully")
    void shouldUseFactoryToProcessTransfer() throws Exception {
        // Given
        PayoutRequest request = createValidRequest();
        PayoutResponse response = PayoutResponse.success("TEST123", "Test Provider", request.getAmount());
        response.setRecipientName(request.getRecipientName());
        response.setCurrency(request.getCurrency());

        // Mock factory behavior
        when(payoutMethodFactory.createProcessor(anyString(), anyString())).thenReturn(mockProcessor);
        when(mockProcessor.processTransfer(any(PayoutRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/transfer/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.transactionId").value("TEST123"))
                .andExpect(jsonPath("$.providerName").value("Test Provider"))
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.recipientName").value("Test User"));
    }

    @Test
    @DisplayName("❌ Controller handles factory exceptions properly")
    void shouldHandleFactoryExceptions() throws Exception {
        // Given
        PayoutRequest request = createValidRequest();

        // Mock factory to throw exception for unsupported combination
        when(payoutMethodFactory.createProcessor(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Unsupported combination: mobile_wallet in mars"));

        // When & Then
        mockMvc.perform(post("/api/transfer/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.errorCode").value("UNSUPPORTED_COMBINATION"));
    }

    @Test
    @DisplayName("🔍 Controller exposes supported methods via factory")
    void shouldExposeSupportedMethods() throws Exception {
        // Given
        Map<String, String> supported = new HashMap<>();
        supported.put("mobile_wallet_philippines", "GCash Philippines");
        supported.put("mobile_wallet_india", "Paytm India");
        supported.put("bank_transfer_philippines", "International Bank Transfer");

        when(payoutMethodFactory.getSupportedCombinations()).thenReturn(supported);

        // When & Then
        mockMvc.perform(get("/api/transfer/supported-methods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mobile_wallet_philippines").value("GCash Philippines"))
                .andExpect(jsonPath("$.mobile_wallet_india").value("Paytm India"))
                .andExpect(jsonPath("$.bank_transfer_philippines").value("International Bank Transfer"));
    }

    @Test
    @DisplayName("✅ Controller validates combination through factory")
    void shouldValidateCombination() throws Exception {
        // Given - Supported combination
        when(payoutMethodFactory.isSupported("mobile_wallet", "philippines")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/transfer/validate")
                        .param("method", "mobile_wallet")
                        .param("country", "philippines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.method").value("mobile_wallet"))
                .andExpect(jsonPath("$.country").value("philippines"))
                .andExpect(jsonPath("$.supported").value(true))
                .andExpect(jsonPath("$.message").value("Combination is supported"));
    }

    @Test
    @DisplayName("❌ Controller handles unsupported combinations")
    void shouldHandleUnsupportedCombination() throws Exception {
        // Given - Unsupported combination
        when(payoutMethodFactory.isSupported("mobile_wallet", "mars")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/transfer/validate")
                        .param("method", "mobile_wallet")
                        .param("country", "mars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.method").value("mobile_wallet"))
                .andExpect(jsonPath("$.country").value("mars"))
                .andExpect(jsonPath("$.supported").value(false))
                .andExpect(jsonPath("$.message").value("Combination is not supported"));
    }

    @Test
    @DisplayName("🛡️ Controller validates request parameters")
    void shouldValidateRequestParameters() throws Exception {
        // Given - Invalid request (missing required fields)
        PayoutRequest invalidRequest = new PayoutRequest();
        invalidRequest.setPayoutMethod(""); // Empty method
        invalidRequest.setDestinationCountry(""); // Empty country
        // Missing amount, currency, etc.

        // When & Then
        mockMvc.perform(post("/api/transfer/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("🔄 Factory creates different processors for different countries")
    void shouldCreateDifferentProcessorsForDifferentCountries() throws Exception {
        // Given - Philippines request
        PayoutRequest philippinesRequest = createValidRequest();
        philippinesRequest.setDestinationCountry("philippines");

        PayoutResponse philippinesResponse = PayoutResponse.success("GC123", "GCash Philippines", philippinesRequest.getAmount());
        philippinesResponse.setRecipientName(philippinesRequest.getRecipientName());
        philippinesResponse.setCurrency("PHP");

        // Mock factory for Philippines
        when(payoutMethodFactory.createProcessor("mobile_wallet", "philippines")).thenReturn(mockProcessor);
        when(mockProcessor.processTransfer(any(PayoutRequest.class))).thenReturn(philippinesResponse);

        // When & Then - Philippines
        mockMvc.perform(post("/api/transfer/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(philippinesRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providerName").value("GCash Philippines"))
                .andExpect(jsonPath("$.transactionId").value("GC123"));

        // Given - India request
        PayoutRequest indiaRequest = createValidRequest();
        indiaRequest.setDestinationCountry("india");
        indiaRequest.setCurrency("INR");

        PayoutResponse indiaResponse = PayoutResponse.success("PTM456", "Paytm India", indiaRequest.getAmount());
        indiaResponse.setRecipientName(indiaRequest.getRecipientName());
        indiaResponse.setCurrency("INR");

        // Mock factory for India
        when(payoutMethodFactory.createProcessor("mobile_wallet", "india")).thenReturn(mockProcessor);
        when(mockProcessor.processTransfer(any(PayoutRequest.class))).thenReturn(indiaResponse);

        // When & Then - India
        mockMvc.perform(post("/api/transfer/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(indiaRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providerName").value("Paytm India"))
                .andExpect(jsonPath("$.transactionId").value("PTM456"));
    }

    private PayoutRequest createValidRequest() {
        PayoutRequest request = new PayoutRequest();
        request.setPayoutMethod("mobile_wallet");
        request.setDestinationCountry("philippines");
        request.setAmount(new BigDecimal("1000"));
        request.setCurrency("PHP");
        request.setRecipientName("Test User");
        request.setRecipientPhone("+639123456789");
        return request;
    }
}