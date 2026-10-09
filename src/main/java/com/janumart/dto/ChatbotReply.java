package com.janumart.dto;

import com.janumart.model.Product;

import java.util.List;

/**
 * Data payload returned by POST /api/v1/chatbot/message.
 *
 * <p>{@code products} always contains real catalog entries fetched through the
 * existing product DAO — the assistant never invents products. {@code mode} is
 * {@code "ai"} when the reply text came from the configured AI provider and
 * {@code "catalog"} when it was built locally from the JanuMart catalog (used
 * when no AI key is configured or the provider is temporarily unavailable).
 */
public class ChatbotReply {

    private String reply;
    private List<Product> products;
    private String mode;

    public ChatbotReply() {
    }

    public ChatbotReply(String reply, List<Product> products, String mode) {
        this.reply = reply;
        this.products = products;
        this.mode = mode;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}