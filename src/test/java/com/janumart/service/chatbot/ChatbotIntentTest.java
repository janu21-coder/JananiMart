package com.janumart.service.chatbot;

import com.janumart.service.chatbot.ChatbotIntent.Kind;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for the chatbot intent parser (pure logic, no database). */
class ChatbotIntentTest {

    @Test
    void watchesUnderBudget() {
        ChatbotIntent i = ChatbotIntent.parse("suggest some watches under ₹1500");
        assertEquals("Watches", i.getCategory());
        assertEquals(new BigDecimal("1500"), i.getMaxPrice());
        assertEquals(Kind.CATALOG, i.getKind());
    }

    @Test
    void walletsUnderBudgetWord() {
        ChatbotIntent i = ChatbotIntent.parse("show me wallets under 800");
        assertEquals("Wallets", i.getCategory());
        assertEquals(new BigDecimal("800"), i.getMaxPrice());
    }

    @Test
    void forWomen() {
        ChatbotIntent i = ChatbotIntent.parse("suggest accessories for women");
        assertEquals("Women", i.getGender());
    }

    @Test
    void giftForHim() {
        ChatbotIntent i = ChatbotIntent.parse("birthday gift ideas for him under 500");
        assertEquals("Gifts", i.getCategory());
        assertEquals("Men", i.getGender());
        assertEquals(new BigDecimal("500"), i.getMaxPrice());
        assertTrue(i.hasFlag("GIFT"));
        assertEquals(Kind.CATALOG, i.getKind());
    }

    @Test
    void affordableBags() {
        ChatbotIntent i = ChatbotIntent.parse("affordable bags please");
        assertEquals("Bags", i.getCategory());
        assertTrue(i.hasFlag("AFFORDABLE"));
        assertEquals(new BigDecimal("1500"), i.getMaxPrice());
        assertEquals(Kind.CATALOG, i.getKind());
    }

    @Test
    void greeting() {
        assertEquals(Kind.GREETING, ChatbotIntent.parse("hello Janu").getKind());
        assertEquals(Kind.GREETING, ChatbotIntent.parse("Hi!").getKind());
    }

    @Test
    void categoriesQuestion() {
        ChatbotIntent i = ChatbotIntent.parse("what categories are available?");
        assertEquals(Kind.CATEGORIES, i.getKind());
    }

    @Test
    void orderTracking() {
        ChatbotIntent i = ChatbotIntent.parse("how do i track my order?");
        assertEquals(Kind.ORDERS, i.getKind());
    }

    @Test
    void cartQuestion() {
        ChatbotIntent i = ChatbotIntent.parse("how do i add a product to my cart");
        assertEquals(Kind.CART, i.getKind());
    }

    @Test
    void checkoutQuestion() {
        ChatbotIntent i = ChatbotIntent.parse("how do I place an order?");
        assertEquals(Kind.CHECKOUT, i.getKind());
    }

    @Test
    void deliveryQuestion() {
        ChatbotIntent i = ChatbotIntent.parse("when will my delivery arrive?");
        assertEquals(Kind.DELIVERY, i.getKind());
    }

    @Test
    void offersQuestion() {
        ChatbotIntent i = ChatbotIntent.parse("are there any discounts right now?");
        assertEquals(Kind.OFFERS, i.getKind());
    }

    @Test
    void unrelatedRedirectsPolitely() {
        ChatbotIntent i = ChatbotIntent.parse("tell me a recipe for pasta");
        assertEquals(Kind.UNRELATED, i.getKind());
    }

    @Test
    void unknownItemStillCatalog() {
        // "shoes" is not a JanuMart category and has no shopping hints — stays CATALOG
        // so the assistant can honestly say nothing matches instead of inventing a product.
        ChatbotIntent i = ChatbotIntent.parse("do you sell shoes?");
        assertEquals(Kind.CATALOG, i.getKind());
        assertNull(i.getCategory());
        assertNotNull(i.getSearchTerm());
    }

    @Test
    void priceRange() {
        ChatbotIntent i = ChatbotIntent.parse("something between 300 and 900");
        assertEquals(new BigDecimal("300"), i.getMinPrice());
        assertEquals(new BigDecimal("900"), i.getMaxPrice());
    }
}