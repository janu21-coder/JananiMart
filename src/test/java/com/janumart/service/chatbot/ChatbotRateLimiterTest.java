package com.janumart.service.chatbot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for the in-memory sliding-window rate limiter. */
class ChatbotRateLimiterTest {

    @Test
    void allowsUpToLimitPerWindow() {
        ChatbotRateLimiter limiter = new ChatbotRateLimiter(2);
        assertTrue(limiter.tryAcquire("k1"));
        assertTrue(limiter.tryAcquire("k1"));
        assertFalse(limiter.tryAcquire("k1"));
    }

    @Test
    void keysAreIndependent() {
        ChatbotRateLimiter limiter = new ChatbotRateLimiter(1);
        assertTrue(limiter.tryAcquire("a"));
        assertTrue(limiter.tryAcquire("b"));
        assertFalse(limiter.tryAcquire("a"));
    }

    @Test
    void acquireThrowsWhenExceeded() {
        ChatbotRateLimiter limiter = new ChatbotRateLimiter(1);
        assertTrue(limiter.tryAcquire("x"));
        assertThrows(RuntimeException.class, () -> limiter.acquire("x"));
    }
}