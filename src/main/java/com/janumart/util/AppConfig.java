package com.janumart.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Loads application.properties from the classpath once.
 *
 * <p>Secrets and deployment overrides may be supplied as environment variables
 * (mapped below); an env var, when present and non-blank, wins over the
 * properties file. Never put real secrets in application.properties.
 */
public final class AppConfig {

    private static Properties props;

    /**
     * Keys that may be overridden by environment variables. The API key must
     * only ever live in the environment, never in a committed file.
     */
    private static final Map<String, String> ENV_OVERRIDES = new HashMap<>();

    static {
        ENV_OVERRIDES.put("ai.enabled", "AI_ENABLED");
        ENV_OVERRIDES.put("ai.apiKey", "AI_PROVIDER_API_KEY");
        ENV_OVERRIDES.put("ai.baseUrl", "AI_BASE_URL");
        ENV_OVERRIDES.put("ai.model", "AI_MODEL");
        ENV_OVERRIDES.put("ai.timeoutSeconds", "AI_TIMEOUT_SECONDS");
    }

    private AppConfig() {
    }

    public static synchronized void load() {
        if (props != null) {
            return;
        }
        props = new Properties();
        try (InputStream in = AppConfig.class.getResourceAsStream("/application.properties")) {
            if (in == null) {
                throw new IllegalStateException("application.properties not found on classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load application.properties", e);
        }
    }

    public static String get(String key, String def) {
        if (props == null) {
            load();
        }
        String envVar = ENV_OVERRIDES.get(key);
        if (envVar != null) {
            String envValue = System.getenv(envVar);
            if (envValue != null && !envValue.isBlank()) {
                return envValue.trim();
            }
        }
        String v = props.getProperty(key);
        return v != null ? v.trim() : def;
    }

    public static String get(String key) {
        return get(key, null);
    }

    /** Raw property bag (already loaded). */
    public static Properties properties() {
        load();
        return props;
    }
}