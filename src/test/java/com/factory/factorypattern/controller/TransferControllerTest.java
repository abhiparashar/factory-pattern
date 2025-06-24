package com.factory.factorypattern.controller;


import com.factory.factorypattern.factory.PayoutMethodFactory;
import com.factory.factorypattern.model.PayoutProcessor;
import com.factory.factorypattern.model.PayoutRequest;
import com.factory.factorypattern.model.PayoutResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
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
 * This version uses the modern approach replacing @MockBean:
 * - Uses @Mock with @TestConfiguration
 * - Avoids deprecated @MockBean
 * - Provides better test isolation
 * - More explicit dependency management
 */
@WebMvcTest(TransferController.class)
@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig
class TransferControllerModernTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private PayoutMethodFactory payoutMethodFactory;

    @Mock
    private PayoutProcessor mockProcessor;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 🔧 MODERN APPROACH: Test Configuration
     * Replaces @MockBean with explicit @TestConfiguration
     */
    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public PayoutMethodFactory payoutMethodFactory() {
            return org.mockito.Mockito.mock(PayoutMethodFactory.class);
        }

        @Bean
        @Primary
        public PayoutProcessor payoutProcessor() {
            return org.mockito.Mockito.mock(PayoutProcessor.class);
        }
    }

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        org.mockito.Mockito.reset(payoutMethodFactory, mockProcessor);
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