package com.janumart.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Payload accepted by POST /api/v1/chatbot/message.
 * The conversation history is optional, bounded and validated server-side.
 */
public class ChatbotRequest {

    private String message;
    private List<ChatMessage> conversationHistory;

    public ChatbotRequest() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ChatMessage> getConversationHistory() {
        return conversationHistory == null ? new ArrayList<>() : conversationHistory;
    }

    public void setConversationHistory(List<ChatMessage> conversationHistory) {
        this.conversationHistory = conversationHistory;
    }
}