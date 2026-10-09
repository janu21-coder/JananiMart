package com.janumart.controller;

import com.janumart.dto.ChatbotReply;
import com.janumart.dto.ChatbotRequest;
import com.janumart.service.chatbot.ChatbotService;
import com.janumart.util.JsonUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * POST /api/v1/chatbot/message — the Janu AI shopping assistant endpoint.
 * Accessible to anonymous visitors (a customer may ask for public catalog
 * help before logging in); per-session rate limiting protects the provider.
 */
public class ChatbotController {

    private final ChatbotService chatbotService = new ChatbotService();

    public void handle(HttpServletRequest req, HttpServletResponse resp, String method, List<String> seg)
            throws IOException {
        boolean messagePath = seg.isEmpty() || (seg.size() == 1 && seg.get(0).equals("message"));
        if (method.equals("POST") && messagePath) {
            ChatbotRequest body = JsonUtil.parse(req, ChatbotRequest.class);
            JsonUtil.ok(resp, "", chatbotService.respond(sessionKey(req), body));
            return;
        }
        if (method.equals("GET") && seg.isEmpty()) {
            // Lightweight availability check; never discloses credentials or configuration details.
            JsonUtil.ok(resp, "", new ChatbotReply(
                    "Janu AI Shopping Assistant is ready.", List.of(), "catalog"));
            return;
        }
        JsonUtil.error(resp, 404, "Chatbot endpoint not found.");
    }

    /** Stable anchor for rate limiting: the session id when present, else the caller IP. */
    private static String sessionKey(HttpServletRequest req) {
        String sid = req.getRequestedSessionId();
        if (sid == null || sid.isBlank()) {
            sid = req.getRemoteAddr();
        }
        return sid;
    }
}