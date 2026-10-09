package com.janumart.model;

import java.time.LocalDateTime;

/**
 * Application user (BUYER, SELLER or ADMIN). Never serialize the password hash.
 */
public class User {

    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private String role;
    private LocalDateTime createdAt;

    public User() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean is(String role) {
        return role != null && role.equals(this.role);
    }

    /** Safe representation for the frontend (never includes the password hash). */
    public java.util.Map<String, Object> safeMap() {
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("email", email);
        m.put("role", role);
        m.put("createdAt", String.valueOf(createdAt));
        return m;
    }
}