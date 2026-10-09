package com.janumart.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import com.janumart.exception.ValidationException;

/**
 * Centralized server-side validation. Never trust frontend-only validation.
 */
public final class Validate {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PINCODE = Pattern.compile("^\\d{6}$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9]{10,15}$");

    private Validate() {
    }

    public static void required(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(field + " is required.");
        }
    }

    public static String clean(String value) {
        return value == null ? null : value.trim();
    }

    /** Returns trimmed value or throws. */
    public static String requireText(String value, String field, int maxLen) {
        required(value, field);
        String v = value.trim();
        if (v.length() > maxLen) {
            throw new ValidationException(field + " must be at most " + maxLen + " characters.");
        }
        return v;
    }

    public static String option(String value, String field, int maxLen) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String v = value.trim();
        if (v.length() > maxLen) {
            throw new ValidationException(field + " must be at most " + maxLen + " characters.");
        }
        return v;
    }

    public static void email(String email) {
        required(email, "Email");
        if (!EMAIL.matcher(email.trim()).matches()) {
            throw new ValidationException("Please enter a valid email address.");
        }
    }

    public static void password(String password) {
        required(password, "Password");
        String p = password.trim();
        if (p.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters.");
        }
        if (p.length() > 72) {
            throw new ValidationException("Password must be at most 72 characters.");
        }
    }

    public static void confirm(String password, String confirm) {
        if (password == null || confirm == null || !password.equals(confirm)) {
            throw new ValidationException("Password and confirm password do not match.");
        }
    }

    public static void price(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Price must be greater than zero.");
        }
        if (price.scale() > 2) {
            throw new ValidationException("Price can have at most two decimals.");
        }
    }

    public static void nonNegativeStock(int stock) {
        if (stock < 0) {
            throw new ValidationException("Stock quantity cannot be negative.");
        }
    }

    public static void positiveQuantity(int qty) {
        if (qty <= 0) {
            throw new ValidationException("Quantity must be a positive number.");
        }
    }

    public static void positiveId(long id, String field) {
        if (id <= 0) {
            throw new ValidationException(field + " must be a valid id.");
        }
    }

    public static void rating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new ValidationException("Rating must be between 1 and 5.");
        }
    }

    public static void pincode(String pincode) {
        required(pincode, "Pincode");
        if (!PINCODE.matcher(pincode.trim()).matches()) {
            throw new ValidationException("Pincode must be a 6 digit number.");
        }
    }

    public static void phone(String phone) {
        required(phone, "Phone number");
        if (!PHONE.matcher(phone.trim()).matches()) {
            throw new ValidationException("Please enter a valid phone number.");
        }
    }

    public static void inOneOf(String value, String field, String... allowed) {
        required(value, field);
        for (String a : allowed) {
            if (a.equals(value)) {
                return;
            }
        }
        throw new ValidationException(field + " has an invalid value.");
    }
}