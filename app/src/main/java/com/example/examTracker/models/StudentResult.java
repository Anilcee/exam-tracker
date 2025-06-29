package com.example.examTracker.models;

import java.util.List;
import java.util.Map;

public class StudentResult {
    private String ogrenci_adi;
    private String ogrenci_numarasi;
    private String okul;
    private List<String> eksik_konular;
    private Map<String, Map<String, Object>> net_bilgileri;
    private int sayfa_numarasi;
    private long tarih;

    public StudentResult() {
        // Firestore için boş constructor gerekli
    }

    public StudentResult(String ogrenci_adi, String ogrenci_numarasi, String okul, 
                        List<String> eksik_konular, Map<String, Map<String, Object>> net_bilgileri, 
                        int sayfa_numarasi, long tarih) {
        this.ogrenci_adi = ogrenci_adi;
        this.ogrenci_numarasi = ogrenci_numarasi;
        this.okul = okul;
        this.eksik_konular = eksik_konular;
        this.net_bilgileri = net_bilgileri;
        this.sayfa_numarasi = sayfa_numarasi;
        this.tarih = tarih;
    }

    // Getter ve Setter metodları
    public String getOgrenci_adi() {
        return ogrenci_adi;
    }

    public void setOgrenci_adi(String ogrenci_adi) {
        this.ogrenci_adi = ogrenci_adi;
    }

    public String getOgrenci_numarasi() {
        return ogrenci_numarasi;
    }

    public void setOgrenci_numarasi(String ogrenci_numarasi) {
        this.ogrenci_numarasi = ogrenci_numarasi;
    }

    public String getOkul() {
        return okul;
    }

    public void setOkul(String okul) {
        this.okul = okul;
    }

    public List<String> getEksik_konular() {
        return eksik_konular;
    }

    public void setEksik_konular(List<String> eksik_konular) {
        this.eksik_konular = eksik_konular;
    }

    public Map<String, Map<String, Object>> getNet_bilgileri() {
        return net_bilgileri;
    }

    public void setNet_bilgileri(Map<String, Map<String, Object>> net_bilgileri) {
        this.net_bilgileri = net_bilgileri;
    }

    public int getSayfa_numarasi() {
        return sayfa_numarasi;
    }

    public void setSayfa_numarasi(int sayfa_numarasi) {
        this.sayfa_numarasi = sayfa_numarasi;
    }

    public long getTarih() {
        return tarih;
    }

    public void setTarih(long tarih) {
        this.tarih = tarih;
    }

    // Net değerini almak için helper metod
    public double getNetBySubject(String subject) {
        if (net_bilgileri != null && net_bilgileri.containsKey(subject)) {
            Map<String, Object> subjectData = net_bilgileri.get(subject);
            if (subjectData != null && subjectData.containsKey("net_sayisi")) {
                Object netValue = subjectData.get("net_sayisi");
                if (netValue instanceof Number) {
                    return ((Number) netValue).doubleValue();
                }
            }
        }
        return 0.0;
    }
} 