package com.janumart.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt password hashing. Plain-text passwords are never stored or logged.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(10));
    }

    public static boolean matches(String plain, String hash) {
        try {
            if (hash == null || hash.isBlank()) {
                return false;
            }
            return BCrypt.checkpw(plain, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}