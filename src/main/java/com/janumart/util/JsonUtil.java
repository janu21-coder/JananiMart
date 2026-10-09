package com.janumart.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.janumart.dto.ApiResponse;

/**
 * JSON helpers on top of Gson.
 */
public final class JsonUtil {

    /**
     * Java 17 blocks reflective access into {@code java.time}, so Gson must be told
     * explicitly how to write java.time values; otherwise every response that carries
     * a Product/Order/Review fails with an InaccessibleObjectException.
     */
    private static final DateTimeFormatter LDT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .serializeNulls()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
            .create();

    private JsonUtil() {
    }

    public static Gson gson() {
        return GSON;
    }

    /** Parses the request body into a {@code Map} for flexible field access. */
    public static Map<String, Object> body(HttpServletRequest req) throws IOException {
        try (Reader reader = req.getReader()) {
            Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
            return GSON.fromJson(reader, mapType);
        }
    }

    public static <T> T parse(Reader reader, Class<T> clazz) {
        return GSON.fromJson(reader, clazz);
    }

    /** Parses a required JSON body from the request. */
    public static <T> T parse(HttpServletRequest req, Class<T> clazz) throws IOException {
        T t = GSON.fromJson(req.getReader(), clazz);
        if (t == null) {
            throw new com.janumart.exception.ValidationException("Request body is required.");
        }
        return t;
    }

    public static void ok(HttpServletResponse resp, String message, Object data) throws IOException {
        write(resp, HttpServletResponse.SC_OK, ApiResponse.success(message, data));
    }

    public static void created(HttpServletResponse resp, String message, Object data) throws IOException {
        write(resp, HttpServletResponse.SC_CREATED, ApiResponse.success(message, data));
    }

    public static void error(HttpServletResponse resp, int status, String message) throws IOException {
        write(resp, status, ApiResponse.error(message));
    }

    public static void write(HttpServletResponse resp, int status, Object payload) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json;charset=UTF-8");
        String json = GSON.toJson(payload);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        resp.setContentLength(bytes.length);
        resp.getOutputStream().write(bytes);
    }

    /** Extracts a string field (or null). */
    public static String str(Map<String, Object> map, String key) {
        Object v = map == null ? null : map.get(key);
        return v == null ? null : String.valueOf(v).trim().isEmpty() ? null : String.valueOf(v).trim();
    }

    public static Integer integer(Map<String, Object> map, String key) {
        Object v = map == null ? null : map.get(key);
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static final class LocalDateTimeAdapter
            implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime src, Type type, JsonSerializationContext ctx) {
            return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.format(LDT));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext ctx)
                throws JsonParseException {
            if (json == null || json.isJsonNull() || json.getAsString().isEmpty()) {
                return null;
            }
            return LocalDateTime.parse(json.getAsString(), LDT);
        }
    }

    private static final class LocalDateAdapter
            implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        @Override
        public JsonElement serialize(LocalDate src, Type type, JsonSerializationContext ctx) {
            return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.toString());
        }

        @Override
        public LocalDate deserialize(JsonElement json, Type type, JsonDeserializationContext ctx)
                throws JsonParseException {
            if (json == null || json.isJsonNull() || json.getAsString().isEmpty()) {
                return null;
            }
            return LocalDate.parse(json.getAsString());
        }
    }

    private static final class LocalTimeAdapter
            implements JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
        @Override
        public JsonElement serialize(LocalTime src, Type type, JsonSerializationContext ctx) {
            return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.toString());
        }

        @Override
        public LocalTime deserialize(JsonElement json, Type type, JsonDeserializationContext ctx)
                throws JsonParseException {
            if (json == null || json.isJsonNull() || json.getAsString().isEmpty()) {
                return null;
            }
            return LocalTime.parse(json.getAsString());
        }
    }
}