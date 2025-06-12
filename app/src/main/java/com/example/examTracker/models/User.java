package com.example.examTracker.models;

import com.google.firebase.firestore.Exclude;

public class User {
    private String email;
    private String role; // "admin" veya "student"
    private String uid;
    private long createdAt;

    public User() {
        // Firestore için boş constructor gerekli
    }

    public User(String email, String role, String uid) {
        this.email = email;
        this.role = role;
        this.uid = uid;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getUid() {
        return uid;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Exclude
    public boolean isAdmin() {
        return "admin".equals(role);
    }

    @Exclude
    public boolean isStudent() {
        return "student".equals(role);
    }
}