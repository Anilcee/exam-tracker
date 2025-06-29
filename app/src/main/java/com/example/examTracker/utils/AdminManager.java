package com.example.examTracker.utils;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminManager {
    
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    
    /**
     * Bir email'i admin yapar
     * @param email Admin yapılacak email
     * @param callback İşlem sonucu callback
     */
    public static void addAdminEmail(String email, AdminCallback callback) {
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("email", email);
        adminData.put("addedAt", System.currentTimeMillis());
        
        db.collection("adminEmails").document(email)
                .set(adminData)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess("Admin başarıyla eklendi: " + email);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError("Admin eklenirken hata: " + e.getMessage());
                    }
                });
    }
    
    /**
     * Bir email'i admin listesinden çıkarır
     * @param email Çıkarılacak email
     * @param callback İşlem sonucu callback
     */
    public static void removeAdminEmail(String email, AdminCallback callback) {
        db.collection("adminEmails").document(email)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess("Admin başarıyla kaldırıldı: " + email);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError("Admin kaldırılırken hata: " + e.getMessage());
                    }
                });
    }
    
    /**
     * İlk admin'i sisteme ekler (bootstrap için)
     */
    public static void initializeFirstAdmin() {
        String firstAdminEmail = "admin@examtracker.com";
        
        // Zaten admin var mı kontrol et
        db.collection("adminEmails").document(firstAdminEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // İlk admin'i ekle
                        addAdminEmail(firstAdminEmail, new AdminCallback() {
                            @Override
                            public void onSuccess(String message) {
                                System.out.println("İlk admin eklendi: " + firstAdminEmail);
                            }
                            
                            @Override
                            public void onError(String error) {
                                System.err.println("İlk admin eklenemedi: " + error);
                            }
                        });
                    }
                });
    }
    
    /**
     * Admin işlemleri için callback interface
     */
    public interface AdminCallback {
        void onSuccess(String message);
        void onError(String error);
    }
} 