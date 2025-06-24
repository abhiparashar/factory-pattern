package com.factory.factorypattern.factory;

import com.factory.factorypattern.model.PayoutProcessor;
import com.factory.factorypattern.service.BankTransferProcessor;
import com.factory.factorypattern.service.GCashProcessor;
import com.factory.factorypattern.service.PaytmProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 🎯 FACTORY PATTERN UNIT TESTS
 *
 * File Location: src/test/java/com/remittance/factory/PayoutMethodFactoryTest.java
 *
 * These tests demonstrate how to properly test the Factory Pattern:
 * 1. Test factory creation logic
 * 2. Test all supported combinations
 * 3. Test error handling for unsupported combinations
 * 4. Test caching mechanism
 * 5. Test factory utility methods
 */
class PayoutMethodFactoryTest {

    @Mock
    private GCashProcessor gCashProcessor;

    @Mock
    private PaytmProcessor paytmProcessor;

    @Mock
    private BankTransferProcessor bankTransferProcessor;

    @InjectMocks
    private PayoutMethodFactory payoutMethodFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup mock behaviors
        when(gCashProcessor.getProviderName()).thenReturn("GCash Philippines");
        when(paytmProcessor.getProviderName()).thenReturn("Paytm India");
        when(bankTransferProcessor.getProviderName()).thenReturn("International Bank Transfer");
    }

    // 🎯 CORE FACTORY PATTERN TESTS

    @Test
    @DisplayName("🇵🇭 Factory creates GCash processor for Philippines mobile wallet")
    void shouldCreateGCashProcessorForPhilippinesMobileWallet() {
        // Given
        String method = "mobile_wallet";
        String country = "philippines";

        // When
        PayoutProcessor processor = payoutMethodFactory.createProcessor(method, country);

        // Then
        assertNotNull(processor);
        assertEquals(gCashProcessor, processor);
        verify(gCashProcessor, atLeastOnce()).getProviderName();
    }

    @Test
    @DisplayName("🇮🇳 Factory creates Paytm processor for India mobile wallet")
    void shouldCreatePaytmProcessorForIndiaMobileWallet() {
        // Given
        String method = "mobile_wallet";
        String country = "india";

        // When
        PayoutProcessor processor = payoutMethodFactory.createProcessor(method, country);

        // Then
        assertNotNull(processor);
        assertEquals(paytmProcessor, processor);
        verify(paytmProcessor, atLeastOnce()).getProviderName();
    }

    @Test
    @DisplayName("🏦 Factory creates Bank Transfer processor for bank transfers")
    void shouldCreateBankTransferProcessorForBankTransfers() {
        // Given
        String method = "bank_transfer";
        String country = "philippines";

        // When
        PayoutProcessor processor = payoutMethodFactory.createProcessor(method, country);

        // Then
        assertNotNull(processor);
        assertEquals(bankTransferProcessor, processor);
        verify(bankTransferProcessor, atLeastOnce()).getProviderName();
    }

    // 🎯 FACTORY PATTERN FLEXIBILITY TESTS

    @Test
    @DisplayName("Factory handles case insensitive inputs")
    void shouldHandleCaseInsensitiveInputs() {
        // Test various case combinations

        // UPPER CASE
        PayoutProcessor processor1 = payoutMethodFactory.createProcessor("MOBILE_WALLET", "PHILIPPINES");
        assertEquals(gCashProcessor, processor1);

        // Mixed Case
        PayoutProcessor processor2 = payoutMethodFactory.createProcessor("Mobile_Wallet", "India");
        assertEquals(paytmProcessor, processor2);

        // lower case
        PayoutProcessor processor3 = payoutMethodFactory.createProcessor("bank_transfer", "philippines");
        assertEquals(bankTransferProcessor, processor3);
    }

    @Test
    @DisplayName("Factory trims whitespace from inputs")
    void shouldTrimWhitespaceFromInputs() {
        // Given
        String methodWithSpaces = "  mobile_wallet  ";
        String countryWithSpaces = "  philippines  ";

        // When
        PayoutProcessor processor = payoutMethodFactory.createProcessor(methodWithSpaces, countryWithSpaces);

        // Then
        assertEquals(gCashProcessor, processor);
    }

    @Test
    @DisplayName("Factory supports country code abbreviations")
    void shouldSupportCountryCodeAbbreviations() {
        // Test short country codes
        PayoutProcessor processor1 = payoutMethodFactory.createProcessor("mobile_wallet", "ph");
        assertEquals(gCashProcessor, processor1);

        PayoutProcessor processor2 = payoutMethodFactory.createProcessor("mobile_wallet", "in");
        assertEquals(paytmProcessor, processor2);
    }

    // 🎯 ERROR HANDLING TESTS

    @Test
    @DisplayName("❌ Factory throws exception for unsupported method")
    void shouldThrowExceptionForUnsupportedMethod() {
        // Given
        String unsupportedMethod = "cryptocurrency";
        String validCountry = "philippines";

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> payoutMethodFactory.createProcessor(unsupportedMethod, validCountry)
        );

        assertTrue(exception.getMessage().contains("Unsupported combination"));
        assertTrue(exception.getMessage().contains("cryptocurrency"));
        assertTrue(exception.getMessage().contains("philippines"));
    }

    @Test
    @DisplayName("❌ Factory throws exception for unsupported country")
    void shouldThrowExceptionForUnsupportedCountry() {
        // Given
        String validMethod = "mobile_wallet";
        String unsupportedCountry = "japan";

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> payoutMethodFactory.createProcessor(validMethod, unsupportedCountry)
        );

        assertTrue(exception.getMessage().contains("mobile_wallet"));
        assertTrue(exception.getMessage().contains("japan"));
    }

    @Test
    @DisplayName("❌ Factory handles null inputs gracefully")
    void shouldHandleNullInputsGracefully() {
        // Test null method
        assertThrows(IllegalArgumentException.class,
                () -> payoutMethodFactory.createProcessor(null, "philippines"));

        // Test null country
        assertThrows(IllegalArgumentException.class,
                () -> payoutMethodFactory.createProcessor("mobile_wallet", null));

        // Test both null
        assertThrows(IllegalArgumentException.class,
                () -> payoutMethodFactory.createProcessor(null, null));
    }

    // 🎯 CACHING MECHANISM TESTS

    @Test
    @DisplayName("🎯 Factory caches created processors")
    void shouldCacheCreatedProcessors() {
        // Given
        String method = "mobile_wallet";
        String country = "philippines";

        // When - Create processor twice
        PayoutProcessor processor1 = payoutMethodFactory.createProcessor(method, country);
        PayoutProcessor processor2 = payoutMethodFactory.createProcessor(method, country);

        // Then - Should return same cached instance
        assertSame(processor1, processor2);
    }

    @Test
    @DisplayName("🧹 Factory cache can be cleared")
    void shouldClearCache() {
        // Given - Create and cache a processor
        payoutMethodFactory.createProcessor("mobile_wallet", "philippines");

        Map<String, Object> statsBefore = payoutMethodFactory.getCacheStats();
        assertTrue((Integer) statsBefore.get("cacheSize") > 0);

        // When - Clear cache
        payoutMethodFactory.clearCache();

        // Then - Cache should be empty
        Map<String, Object> statsAfter = payoutMethodFactory.getCacheStats();
        assertEquals(0, statsAfter.get("cacheSize"));
    }

    @Test
    @DisplayName("📊 Factory provides cache statistics")
    void shouldProvideCacheStatistics() {
        // Given - Create some processors
        payoutMethodFactory.createProcessor("mobile_wallet", "philippines");
        payoutMethodFactory.createProcessor("mobile_wallet", "india");

        // When
        Map<String, Object> stats = payoutMethodFactory.getCacheStats();

        // Then
        assertEquals(2, stats.get("cacheSize"));
        assertTrue(stats.containsKey("cachedKeys"));
    }

    // 🎯 UTILITY METHOD TESTS

    @Test
    @DisplayName("🔍 Factory provides supported combinations")
    void shouldProvideSupportedCombinations() {
        // When
        Map<String, String> supported = payoutMethodFactory.getSupportedCombinations();

        // Then
        assertFalse(supported.isEmpty());
        assertTrue(supported.containsKey("mobile_wallet_philippines"));
        assertTrue(supported.containsKey("mobile_wallet_india"));
        assertTrue(supported.containsKey("bank_transfer_india"));

        assertEquals("GCash Philippines", supported.get("mobile_wallet_philippines"));
        assertEquals("Paytm India", supported.get("mobile_wallet_india"));
    }

    @Test
    @DisplayName("✅ Factory validates supported combinations correctly")
    void shouldValidateSupportedCombinations() {
        // Supported combinations
        assertTrue(payoutMethodFactory.isSupported("mobile_wallet", "philippines"));
        assertTrue(payoutMethodFactory.isSupported("mobile_wallet", "india"));
        assertTrue(payoutMethodFactory.isSupported("bank_transfer", "philippines"));

        // Unsupported combinations
        assertFalse(payoutMethodFactory.isSupported("mobile_wallet", "japan"));
        assertFalse(payoutMethodFactory.isSupported("cryptocurrency", "philippines"));
        assertFalse(payoutMethodFactory.isSupported("invalid", "invalid"));
    }

    // 🎯 INTEGRATION STYLE TESTS

    @Test
    @DisplayName("🔄 Factory works with all supported combinations")
    void shouldWorkWithAllSupportedCombinations() {
        // Define all expected combinations
        String[][] combinations = {
                {"mobile_wallet", "philippines"},
                {"mobile_wallet", "ph"},
                {"mobile_wallet", "india"},
                {"mobile_wallet", "in"},
                {"digital_wallet", "india"},
                {"bank_transfer", "philippines"},
                {"bank_transfer", "india"},
                {"wire_transfer", "philippines"}
        };

        // Test each combination
        for (String[] combo : combinations) {
            String method = combo[0];
            String country = combo[1];

            assertDoesNotThrow(() -> {
                PayoutProcessor processor = payoutMethodFactory.createProcessor(method, country);
                assertNotNull(processor, "Processor should not be null for " + method + " in " + country);
            });
        }
    }
}
