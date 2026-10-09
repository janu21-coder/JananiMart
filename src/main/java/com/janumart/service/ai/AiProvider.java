package com.janumart.service.ai;

import com.janumart.dto.ChatMessage;

import java.util.List;

/**
 * Abstraction over a chat AI provider. Implementations must never log or
 * otherwise expose the API key, and must treat all inputs (including product
 * descriptions) as untrusted data rather than instructions.
 */
public interface AiProvider {

    /**
     * True when a usable provider (API key + endpoint) is configured in the
     * backend environment. The key itself is never exposed.
     */
    boolean isConfigured();

    /**
     * Asks the provider for a single assistant reply built from the given
     * system prompt and a bounded, sanitized conversation history.
     *
     * @throws AiException when the provider cannot be reached or replies with
     *                     unusable output
     */
    String complete(String systemPrompt, List<ChatMessage> history) throws AiException;
}