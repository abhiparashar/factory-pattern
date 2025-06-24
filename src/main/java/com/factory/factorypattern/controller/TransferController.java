package com.factory.factorypattern.controller;

import com.factory.factorypattern.factory.PayoutMethodFactory;
import com.factory.factorypattern.model.PayoutProcessor;
import com.factory.factorypattern.model.PayoutRequest;
import com.factory.factorypattern.model.PayoutResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 🎯 FACTORY PATTERN - Client/Consumer
 *
 * This controller demonstrates how to use the Factory Pattern:
 * 1. Depends only on the factory, not concrete processors
 * 2. Uses factory to get appropriate processor
 * 3. Works with processor through interface only
 * 4. Doesn't need to know which concrete processor is returned
 *
 * FACTORY PATTERN BENEFITS IN CONTROLLER:
 * - Clean, simple code with no complex if-else logic
 * - Easy to test (can mock factory)
 * - Automatic support for new processors when added to factory
 * - Separation of concerns: controller handles HTTP, factory handles creation
 */
@RestController
@RequestMapping("/api/transfer")
public class TransferController {

    @Autowired
    private PayoutMethodFactory payoutFactory;

    /**
     * 🎯 MAIN ENDPOINT - Demonstrates Factory Pattern Usage
     *
     * Notice how simple this is:
     * 1. Ask factory for processor
     * 2. Use processor through interface
     * 3. Return result
     *
     * No complex logic, no knowledge of concrete classes!
     */
    @PostMapping("/send")
    public ResponseEntity<PayoutResponse> sendMoney(@Valid @RequestBody PayoutRequest request) {
        try {
            System.out.println("📨 Received transfer request: " + request);

            // 🏭 FACTORY PATTERN IN ACTION
            // Factory handles all the complex creation logic
            PayoutProcessor processor = payoutFactory.createProcessor(
                    request.getPayoutMethod(),
                    request.getDestinationCountry()
            );

            // 🔧 INTERFACE USAGE
            // Work with processor through interface - don't care about concrete type
            PayoutResponse response = processor.processTransfer(request);

            // Return appropriate HTTP status based on response
            HttpStatus status = response.getStatus() == PayoutResponse.Status.SUCCESS ?
                    HttpStatus.OK : HttpStatus.BAD_REQUEST;

            return new ResponseEntity<>(response, status);

        } catch (IllegalArgumentException e) {
            // Handle unsupported method/country combinations
            System.err.println("❌ Unsupported combination: " + e.getMessage());

            PayoutResponse errorResponse = PayoutResponse.failed(
                    "Unsupported payment method or country: " + e.getMessage(),
                    "System",
                    "UNSUPPORTED_COMBINATION"
            );

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            // Handle unexpected errors
            System.err.println("❌ Unexpected error: " + e.getMessage());

            PayoutResponse errorResponse = PayoutResponse.failed(
                    "Internal server error occurred",
                    "System",
                    "INTERNAL_ERROR"
            );

            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 🔍 UTILITY ENDPOINT: Get supported combinations
     * Demonstrates factory introspection capabilities
     */
    @GetMapping("/supported-methods")
    public ResponseEntity<Map<String, String>> getSupportedMethods() {
        try {
            Map<String, String> supported = payoutFactory.getSupportedCombinations();
            return ResponseEntity.ok(supported);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🔍 VALIDATION ENDPOINT: Check if combination is supported
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateCombination(
            @RequestParam String method,
            @RequestParam String country) {

        boolean supported = payoutFactory.isSupported(method, country);

        Map<String, Object> result = Map.of(
                "method", method,
                "country", country,
                "supported", supported,
                "message", supported ? "Combination is supported" : "Combination is not supported"
        );

        return ResponseEntity.ok(result);
    }

    /**
     * 📊 ADMIN ENDPOINT: Get factory statistics
     */
    @GetMapping("/factory-stats")
    public ResponseEntity<Map<String, Object>> getFactoryStats() {
        try {
            Map<String, Object> stats = payoutFactory.getCacheStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🧹 ADMIN ENDPOINT: Clear factory cache
     */
    @PostMapping("/clear-cache")
    public ResponseEntity<String> clearCache() {
        try {
            payoutFactory.clearCache();
            return ResponseEntity.ok("Factory cache cleared successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to clear cache");
        }
    }
}