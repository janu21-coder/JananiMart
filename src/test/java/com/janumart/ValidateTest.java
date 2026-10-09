package com.janumart;

import org.junit.jupiter.api.Test;

import com.janumart.exception.ValidationException;
import com.janumart.util.Validate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidateTest {

    @Test
    void emails() {
        assertDoesNotThrow(() -> Validate.email("buyer@janumart.com"));
        assertThrows(ValidationException.class, () -> Validate.email("not-an-email"));
        assertThrows(ValidationException.class, () -> Validate.email(""));
    }

    @Test
    void passwordRules() {
        assertDoesNotThrow(() -> Validate.password("secret123"));
        assertThrows(ValidationException.class, () -> Validate.password("123"));
    }

    @Test
    void priceRules() {
        assertDoesNotThrow(() -> Validate.price(new BigDecimal("999.99")));
        assertThrows(ValidationException.class, () -> Validate.price(new BigDecimal("-1")));
        assertThrows(ValidationException.class, () -> Validate.price(BigDecimal.ZERO));
    }

    @Test
    void pincodeAndPhone() {
        assertDoesNotThrow(() -> Validate.pincode("560038"));
        assertThrows(ValidationException.class, () -> Validate.pincode("56003"));
        assertDoesNotThrow(() -> Validate.phone("9876543210"));
        assertThrows(ValidationException.class, () -> Validate.phone("123"));
    }

    @Test
    void ratingRange() {
        assertDoesNotThrow(() -> Validate.rating(1));
        assertDoesNotThrow(() -> Validate.rating(5));
        assertThrows(ValidationException.class, () -> Validate.rating(0));
        assertThrows(ValidationException.class, () -> Validate.rating(6));
    }
}