package com.factory.factorypattern.factory;
import com.factory.factorypattern.model.PayoutProcessor;
import com.factory.factorypattern.service.BankTransferProcessor;
import com.factory.factorypattern.service.GCashProcessor;
import com.factory.factorypattern.service.PaytmProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 🎯 FACTORY PATTERN - The Factory Class
 *
 * This is the heart of the Factory Pattern implementation.
 *
 * FACTORY PATTERN RESPONSIBILITIES:
 * 1. Encapsulates object creation logic
 * 2. Provides a single point for creating processors
 * 3. Hides concrete class instantiation from clients
 * 4. Makes adding new processors easy (just add to switch/map)
 * 5. Reduces coupling between client and concrete classes
 *
 * KEY BENEFITS DEMONSTRATED:
 * - Single Responsibility: Only handles processor creation
 * - Open/Closed: Open for extension (new processors), closed for modification
 * - Dependency Inversion: Client depends on factory interface, not concrete classes
 */
@Component
public class PayoutMethodFactory {

    // Spring will inject all PayoutProcessor implementations
    @Autowired
    private GCashProcessor gCashProcessor;

    @Autowired
    private PaytmProcessor paytmProcessor;

    @Autowired
    private BankTransferProcessor bankTransferProcessor;

    // Cache for processor mappings (performance optimization)
    private final Map<String, PayoutProcessor> processorCache = new HashMap<>();

    /**
     * 🎯 MAIN FACTORY METHOD
     *
     * This method demonstrates the core Factory Pattern concept:
     * - Takes parameters that determine which object to create
     * - Uses creation logic to select appropriate concrete class
     * - Returns interface type (PayoutProcessor) not concrete type
     * - Client code doesn't need to know which concrete class is returned
     */
    public PayoutProcessor createProcessor(String method, String country) {
        // Normalize inputs
        String normalizedMethod = method != null ? method.toLowerCase().trim() : "";
        String normalizedCountry = country != null ? country.toLowerCase().trim() : "";

        // Create cache key
        String cacheKey = normalizedMethod + "_" + normalizedCountry;

        // Check cache first (performance optimization)
        if (processorCache.containsKey(cacheKey)) {
            System.out.println("🎯 Factory: Retrieved from cache - " + cacheKey);
            return processorCache.get(cacheKey);
        }

        // 🏭 FACTORY CREATION LOGIC
        PayoutProcessor processor = createProcessorInternal(normalizedMethod, normalizedCountry);

        // Cache the result
        processorCache.put(cacheKey, processor);

        System.out.println("🏭 Factory created: " + processor.getProviderName() +
                " for " + normalizedMethod + " in " + normalizedCountry);

        return processor;
    }

    /**
     * Internal creation logic - this is where the magic happens!
     *
     * 🎯 FACTORY PATTERN CORE LOGIC:
     * - Uses method + country combination to determine processor
     * - Switch statement provides clear, maintainable creation rules
     * - Easy to extend: just add new case for new processor
     * - Fails fast with clear error message for unsupported combinations
     */
    private PayoutProcessor createProcessorInternal(String method, String country) {
        // Create decision key
        String key = method + "_" + country;

        return switch (key) {
            // 🇵🇭 Philippines Mobile Wallet -> GCash
            case "mobile_wallet_philippines", "mobile_wallet_ph" -> {
                System.out.println("🏭 Creating GCash processor for Philippines");
                yield gCashProcessor;
            }

            // 🇮🇳 India Mobile/Digital Wallet -> Paytm
            case "mobile_wallet_india", "mobile_wallet_in",
                 "digital_wallet_india", "digital_wallet_in" -> {
                System.out.println("🏭 Creating Paytm processor for India");
                yield paytmProcessor;
            }

            // 🏦 Bank Transfer for multiple countries
            case "bank_transfer_india", "bank_transfer_in",
                 "bank_transfer_philippines", "bank_transfer_ph",
                 "bank_transfer_bangladesh", "bank_transfer_bd",
                 "bank_transfer_nepal", "bank_transfer_np",
                 "bank_transfer_sri lanka", "bank_transfer_lk",
                 "wire_transfer_india", "wire_transfer_in",
                 "wire_transfer_philippines", "wire_transfer_ph" -> {
                System.out.println("🏭 Creating Bank Transfer processor for " + country);
                yield bankTransferProcessor;
            }

            // ❌ Unsupported combination
            default -> {
                String errorMsg = "Unsupported combination: " + method + " in " + country;
                System.err.println("❌ Factory Error: " + errorMsg);
                throw new IllegalArgumentException(errorMsg +
                        ". Supported: mobile_wallet(ph,in), bank_transfer(multiple countries)");
            }
        };
    }

    /**
     * 🔍 UTILITY METHOD: Get all supported combinations
     * Helpful for API documentation and validation
     */
    public Map<String, String> getSupportedCombinations() {
        Map<String, String> supported = new HashMap<>();

        // Mobile Wallets
        supported.put("mobile_wallet_philippines", "GCash Philippines");
        supported.put("mobile_wallet_india", "Paytm India");
        supported.put("digital_wallet_india", "Paytm India");

        // Bank Transfers
        supported.put("bank_transfer_india", "International Bank Transfer");
        supported.put("bank_transfer_philippines", "International Bank Transfer");
        supported.put("bank_transfer_bangladesh", "International Bank Transfer");
        supported.put("bank_transfer_nepal", "International Bank Transfer");
        supported.put("bank_transfer_sri_lanka", "International Bank Transfer");

        return supported;
    }

    /**
     * 🔍 VALIDATION METHOD: Check if combination is supported
     * Client can validate before calling createProcessor
     */
    public boolean isSupported(String method, String country) {
        try {
            createProcessor(method, country);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 🧹 CACHE MANAGEMENT: Clear cache (useful for testing)
     */
    public void clearCache() {
        processorCache.clear();
        System.out.println("🧹 Factory cache cleared");
    }

    /**
     * 📊 CACHE STATS: Get cache information
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", processorCache.size());
        stats.put("cachedKeys", processorCache.keySet());
        return stats;
    }
}
