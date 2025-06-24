package com.factory.factorypattern.model;

/**
 * 🎯 FACTORY PATTERN - Product Interface
 *
 * This interface defines the contract that all concrete processors must implement.
 * The Factory will return objects of this type, allowing client code to work
 * with any processor without knowing the specific implementation.
 *
 * Key Factory Pattern Benefits:
 * - Uniform interface for all processors
 * - Client code depends on abstraction, not concrete classes
 * - Easy to add new processors without changing existing code
 */
public interface PayoutProcessor {

    /**
     * Main processing method - all processors must implement this
     */
    PayoutResponse processTransfer(PayoutRequest request);

    /**
     * Validation method to check if processor supports given combination
     */
    boolean isSupported(String country, String method);

    /**
     * Provider identification
     */
    String getProviderName();

    /**
     * Get supported countries (for future factory enhancements)
     */
    default String[] getSupportedCountries() {
        return new String[0];
    }

    /**
     * Get supported methods (for future factory enhancements)
     */
    default String[] getSupportedMethods() {
        return new String[0];
    }

    /**
     * Validation hook that can be overridden
     */
    default boolean validateRequest(PayoutRequest request) {
        return request != null &&
                request.getAmount() != null &&
                request.getAmount().compareTo(java.math.BigDecimal.ZERO) > 0;
    }
}
