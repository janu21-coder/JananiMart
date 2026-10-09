package com.janumart.service.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.janumart.dto.ChatMessage;
import com.janumart.util.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * Chat-completions client for any OpenAI-compatible provider (OpenAI, Groq,
 * OpenRouter, local Ollama, ...). Uses the JDK HTTP client — no extra SDK.
 *
 * <p>Configuration (all optional except the API key):</p>
 * <ul>
 *   <li>{@code AI_PROVIDER_API_KEY} (env, required) — never stored in the repo</li>
 *   <li>{@code AI_BASE_URL} / {@code ai.baseUrl} — default {@code https://api.openai.com/v1}</li>
 *   <li>{@code AI_MODEL} / {@code ai.model} — default {@code gpt-4o-mini}</li>
 *   <li>{@code AI_TIMEOUT_SECONDS} / {@code ai.timeoutSeconds} — default {@code 20}</li>
 *   <li>{@code AI_ENABLED} / {@code ai.enabled} — default {@code false}</li>
 * </ul>
 */
public class OpenAiCompatibleProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleProvider.class);

    private final boolean enabled;
    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final Duration timeout;
    private final HttpClient client;

    public OpenAiCompatibleProvider() {
        this.enabled = Boolean.parseBoolean(AppConfig.get("ai.enabled", "false"));
        this.apiKey = AppConfig.get("ai.apiKey", "");
        this.baseUrl = trimTrailingSlash(AppConfig.get("ai.baseUrl", "https://api.openai.com/v1"));
        this.model = AppConfig.get("ai.model", "gpt-4o-mini");
        int seconds = parseInt(AppConfig.get("ai.timeoutSeconds", "20"), 20);
        this.timeout = Duration.ofSeconds(seconds);
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        if (enabled) {
            log.info("AI provider configured (model={}, baseUrl={}, timeout={}s).",
                    model, baseUrl, seconds);
        }
    }

    @Override
    public boolean isConfigured() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    @Override
    public String complete(String systemPrompt, List<ChatMessage> history) throws AiException {
        if (!isConfigured()) {
            throw new AiException("AI provider is not configured (set AI_PROVIDER_API_KEY).");
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("model", model);
        JsonArray messages = new JsonArray();
        JsonObject system = new JsonObject();
        system.addProperty("role", "system");
        system.addProperty("content", systemPrompt);
        messages.add(system);
        for (ChatMessage m : history) {
            JsonObject msg = new JsonObject();
            msg.addProperty("role", m.getRole());
            msg.addProperty("content", m.getContent());
            messages.add(msg);
        }
        payload.add("messages", messages);
        payload.addProperty("temperature", 0.4);
        payload.addProperty("max_tokens", 500);

        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Accept", "application/json")
                    .timeout(timeout)
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                    .build();
        } catch (IllegalArgumentException e) {
            throw new AiException("AI provider base URL is invalid.", e);
        }

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiException("AI request was interrupted.", e);
        } catch (IOException e) {
            throw new AiException("Could not reach the AI provider (network).", e);
        }

        int status = response.statusCode();
        if (status != 200) {
            throw new AiException(providerError(status));
        }

        String reply = parseContent(response.body());
        if (reply == null || reply.isBlank()) {
            throw new AiException("AI provider returned an empty response.");
        }
        return reply;
    }

    /** Extracts {@code choices[0].message.content}. */
    private static String parseContent(String body) {
        try {
            JsonObject root = JsonParser.parseString(body).getAsJsonObject();
            JsonArray choices = root.getAsJsonArray("choices");
            if (choices == null || choices.size() == 0) {
                return null;
            }
            JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            return message == null || message.get("content") == null
                    ? null
                    : message.get("content").getAsString().trim();
        } catch (RuntimeException e) {
            log.debug("Malformed AI provider response ignored.");
            return null;
        }
    }

    private static String providerError(int status) {
        if (status == 401 || status == 403) {
            return "AI provider rejected the API key (authentication failed).";
        }
        if (status == 429) {
            return "AI provider rate limit reached. Please try again shortly.";
        }
        if (status >= 500) {
            return "AI provider is temporarily unavailable.";
        }
        return "AI provider returned an error (HTTP " + status + ").";
    }

    private static String trimTrailingSlash(String url) {
        return url == null ? null : url.replaceAll("/+$", "");
    }

    private static int parseInt(String v, int def) {
        try {
            return v == null ? def : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}