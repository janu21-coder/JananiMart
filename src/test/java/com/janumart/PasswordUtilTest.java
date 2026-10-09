package com.janumart;

import org.junit.jupiter.api.Test;

import com.janumart.util.PasswordUtil;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    void hashesAreSaltedAndVerifiable() {
        String hash1 = PasswordUtil.hash("Buyer@123");
        String hash2 = PasswordUtil.hash("Buyer@123");

        // BCrypt output is salted: the same password produces a different hash each time.
        assertNotEquals(hash1, hash2);
        assertTrue(PasswordUtil.matches("Buyer@123", hash1));
        assertTrue(PasswordUtil.matches("Buyer@123", hash2));
        assertFalse(PasswordUtil.matches("wrong", hash1));
        // Plain text is never stored.
        assertFalse(hash1.contains("Buyer@123"));
    }

    @Test
    void invalidHashCannotCrash() {
        assertFalse(PasswordUtil.matches("anything", "not-a-bcrypt-hash"));
    }
}