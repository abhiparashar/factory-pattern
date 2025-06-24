package com.factory.factorypattern.service;

import com.factory.factorypattern.model.PayoutRequest;
import com.factory.factorypattern.model.PayoutResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@Service
class PaytmProcessorTest {

    private PaytmProcessor paytmProcessor;

    @BeforeEach
    void setUp() {
        paytmProcessor = new PaytmProcessor();
    }

    @Test
    @DisplayName("🇮🇳 Paytm processes valid request successfully")
    void shouldProcessValidPaytmRequest() {
        // Given
        PayoutRequest request = createValidPaytmRequest();

        // When
        PayoutResponse response = paytmProcessor.processTransfer(request);

        // Then
        assertEquals(PayoutResponse.Status.SUCCESS, response.getStatus());
        assertEquals("Paytm India", response.getProviderName());
        assertTrue(response.getTransactionId().startsWith("PTM"));
    }

    @Test
    @DisplayName("✅ Paytm supports India and both wallet types")
    void shouldSupportIndiaAndWalletTypes() {
        assertTrue(paytmProcessor.isSupported("india", "mobile_wallet"));
        assertTrue(paytmProcessor.isSupported("in", "mobile_wallet"));
        assertTrue(paytmProcessor.isSupported("india", "digital_wallet"));

        assertFalse(paytmProcessor.isSupported("philippines", "mobile_wallet"));
    }

    private PayoutRequest createValidPaytmRequest() {
        PayoutRequest request = new PayoutRequest();
        request.setPayoutMethod("mobile_wallet");
        request.setDestinationCountry("india");
        request.setAmount(new BigDecimal("5000"));
        request.setCurrency("INR");
        request.setRecipientName("Raj Kumar");
        request.setRecipientPhone("+919876543210");
        return request;
    }
}
