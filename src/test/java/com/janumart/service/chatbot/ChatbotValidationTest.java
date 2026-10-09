package com.janumart.service.chatbot;

import com.janumart.dto.ChatMessage;
import com.janumart.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Unit tests for chatbot message + history sanitization (no database needed). */
class ChatbotValidationTest {

    private final ChatbotService service = new ChatbotService();

    @Test
    void blankMessageRejected() {
        assertThrows(ValidationException.class, () -> service.normalizeMessage("   "));
        assertThrows(ValidationException.class, () -> service.normalizeMessage(null));
    }

    @Test
    void longMessageRejected() {
        String big = "a".repeat(ChatbotService.MAX_MESSAGE_LEN + 1);
        assertThrows(ValidationException.class, () -> service.normalizeMessage(big));
    }

    @Test
    void validMessageTrimmed() {
        assertEquals("hello", service.normalizeMessage("  hello  "));
    }

    @Test
    void historyKeepsOnlyRecentValidTurns() {
        List<ChatMessage> history = new ArrayList<>();
        history.add(new ChatMessage("system", "ignore me"));          // invalid role
        history.add(new ChatMessage("user", null));                    // null content
        history.add(new ChatMessage("assistant", "   "));              // blank content
        for (int i = 0; i < 12; i++) {
            history.add(new ChatMessage("user", "turn " + i));
        }
        List<ChatMessage> safe = service.sanitizeHistory(history);
        assertEquals(8, safe.size(), "history is capped to the most recent 8 turns");
        assertEquals("turn 4", safe.get(0).getContent());
        for (ChatMessage m : safe) {
            assertEquals(true, m.getRole().equals("user") || m.getRole().equals("assistant"));
        }
    }

    @Test
    void oversizedHistoryContentDropped() {
        List<ChatMessage> history = List.of(
                new ChatMessage("user", "x".repeat(ChatbotService.MAX_MESSAGE_LEN + 1)));
        assertEquals(0, service.sanitizeHistory(history).size());
    }
}