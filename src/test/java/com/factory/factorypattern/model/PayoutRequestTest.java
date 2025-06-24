package com.factory.factorypattern.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PayoutRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("✅ Valid request passes validation")
    void shouldPassValidationForValidRequest() {
        // Given
        PayoutRequest request = new PayoutRequest("mobile_wallet", "philippines",
                new BigDecimal("1000"), "PHP", "Test User");
        request.setRecipientPhone("+639123456789");

        // When
        Set<ConstraintViolation<PayoutRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("❌ Invalid request fails validation")
    void shouldFailValidationForInvalidRequest() {
        // Given - Empty request
        PayoutRequest request = new PayoutRequest();

        // When
        Set<ConstraintViolation<PayoutRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
    }
}
