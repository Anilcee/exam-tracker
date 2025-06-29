package com.example.examTracker.models;

import java.util.List;

public class WeakTopicBySubject {
    private String subject;
    private List<String> topics;

    public WeakTopicBySubject() {
        // Firestore için boş constructor
    }

    public WeakTopicBySubject(String subject, List<String> topics) {
        this.subject = subject;
        this.topics = topics;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }
} 