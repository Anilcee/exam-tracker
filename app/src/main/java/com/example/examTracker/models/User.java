package com.example.examTracker.models;

import com.google.firebase.firestore.Exclude;

public class User {
    private String email;
    private String role; // "admin" veya "student"
    private String uid;
    private String studentNumber; // Öğrenci numarası
    private String firstName; // Ad
    private String lastName; // Soyad
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

    public User(String email, String role, String uid, String studentNumber) {
        this.email = email;
        this.role = role;
        this.uid = uid;
        this.studentNumber = studentNumber;
        this.createdAt = System.currentTimeMillis();
    }

    public User(String email, String role, String uid, String studentNumber, String firstName, String lastName) {
        this.email = email;
        this.role = role;
        this.uid = uid;
        this.studentNumber = studentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
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

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
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

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    @Exclude
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return "";
    }
}