package com.janumart.service.chatbot;

import com.janumart.exception.AppException;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight, in-memory per-session sliding-window rate limiter for chatbot
 * messages. Bounded: stale entries are pruned and the map is capped so it can
 * never grow without limit.
 */
public class ChatbotRateLimiter {

    private static final long WINDOW_MS = 60_000L;
    private static final int MAX_KEYS = 5_000;

    private final int maxPerMinute;
    private final Map<String, ArrayDeque<Long>> hits = new ConcurrentHashMap<>();

    public ChatbotRateLimiter() {
        this(30);
    }

    public ChatbotRateLimiter(int maxPerMinute) {
        this.maxPerMinute = maxPerMinute;
    }

    /** Throws a 429 AppException when the caller has exceeded the limit. */
    public void acquire(String key) {
        if (hits.size() > MAX_KEYS) {
            prune();
        }
        if (!tryAcquire(key)) {
            throw new AppException(429, "You're sending messages too fast. Please wait a few seconds.");
        }
    }

    /** @return true when the call is within the window (testable without HTTP). */
    public boolean tryAcquire(String key) {
        if (key == null || key.isBlank()) {
            key = "anonymous";
        }
        long now = System.currentTimeMillis();
        ArrayDeque<Long> queue = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (queue) {
            while (!queue.isEmpty() && now - queue.peekFirst() > WINDOW_MS) {
                queue.removeFirst();
            }
            if (queue.size() >= maxPerMinute) {
                return false;
            }
            queue.addLast(now);
            return true;
        }
    }

    private void prune() {
        long now = System.currentTimeMillis();
        hits.entrySet().removeIf(e -> {
            ArrayDeque<Long> q = e.getValue();
            synchronized (q) {
                while (!q.isEmpty() && now - q.peekFirst() > WINDOW_MS) {
                    q.removeFirst();
                }
                return q.isEmpty();
            }
        });
    }
}