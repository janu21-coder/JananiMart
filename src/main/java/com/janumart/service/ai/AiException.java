package com.janumart.service.ai;

/**
 * A failure talking to the AI provider (missing key, auth error, rate limit,
 * timeout, network or malformed output). Never contains secrets; the message
 * is safe to log.
 */
public class AiException extends RuntimeException {

    public AiException(String message) {
        super(message);
    }

    public AiException(String message, Throwable cause) {
        super(message, cause);
    }
}