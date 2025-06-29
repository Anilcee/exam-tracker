package com.example.examTracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examTracker.models.StudentResult;
import com.example.examTracker.models.WeakTopicBySubject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExamResultsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private Spinner spinnerExams;
    private ProgressBar progressBarLoading;
    private TextView textViewNoExams;
    private ScrollView scrollViewResults;

    private TextView textViewExamDate;
    private TextView textViewExamName;

    private RecyclerView recyclerViewNetResults;
    private TextView textViewNoNetResults;
    private NetResultAdapter netResultAdapter;

    private RecyclerView recyclerViewWeakTopics;
    private TextView textViewNoWeakTopics;
    private WeakTopicsBySubjectAdapter weakTopicsAdapter;

    private List<StudentResult> examResults = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exam_results, container, false);

        // View'ları bağla
        spinnerExams = view.findViewById(R.id.spinnerExams);
        progressBarLoading = view.findViewById(R.id.progressBarLoading);
        textViewNoExams = view.findViewById(R.id.textViewNoExams);
        scrollViewResults = view.findViewById(R.id.scrollViewResults);

        textViewExamDate = view.findViewById(R.id.textViewExamDate);
        textViewExamName = view.findViewById(R.id.textViewExamName);

        recyclerViewNetResults = view.findViewById(R.id.recyclerViewNetResults);
        textViewNoNetResults = view.findViewById(R.id.textViewNoNetResults);

        recyclerViewWeakTopics = view.findViewById(R.id.recyclerViewWeakTopics);
        textViewNoWeakTopics = view.findViewById(R.id.textViewNoWeakTopics);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerViews();
        setupSpinner();
        loadExamResults();
    }

    private void setupRecyclerViews() {
        // Net sonuçları RecyclerView
        recyclerViewNetResults.setLayoutManager(new LinearLayoutManager(getContext()));
        netResultAdapter = new NetResultAdapter(new ArrayList<>());
        recyclerViewNetResults.setAdapter(netResultAdapter);

        // Eksik konular RecyclerView (derse göre gruplanmış)
        recyclerViewWeakTopics.setLayoutManager(new LinearLayoutManager(getContext()));
        weakTopicsAdapter = new WeakTopicsBySubjectAdapter(new ArrayList<>(), this::onTopicClick);
        recyclerViewWeakTopics.setAdapter(weakTopicsAdapter);
    }

    private void onTopicClick(String subject, String topic) {
        Log.d("TOPIC_CLICK", "=== KONU TİKLAMA ===");
        
        try {
            Log.d("TOPIC_CLICK", "1. Metod başladı");
            Log.d("TOPIC_CLICK", "2. Subject: " + subject);
            Log.d("TOPIC_CLICK", "3. Topic: " + topic);
            
            if (getContext() != null) {
                // QuestionActivity'yi aç
                Intent intent = new Intent(getContext(), QuestionActivity.class);
                intent.putExtra("subject", subject);
                intent.putExtra("topic", topic);
                startActivity(intent);
                
                Log.d("TOPIC_CLICK", "QuestionActivity başlatıldı");
            }
            
        } catch (Exception e) {
            Log.e("TOPIC_CLICK", "Hata: " + e.getMessage(), e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Hata: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupSpinner() {
        // Spinner adapter
        spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExams.setAdapter(spinnerAdapter);

        // Spinner seçim dinleyicisi
        spinnerExams.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < examResults.size()) {
                    displayExamResult(examResults.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                hideResultDetails();
            }
        });
    }

    private void loadExamResults() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        progressBarLoading.setVisibility(View.VISIBLE);
        textViewNoExams.setVisibility(View.GONE);
        scrollViewResults.setVisibility(View.GONE);

        // Önce öğrenci numarasını al
        firestore.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String studentNumber = task.getResult().getString("studentNumber");
                        if (studentNumber != null && !studentNumber.isEmpty()) {
                            loadExamsByStudentNumber(studentNumber);
                        } else {
                            showNoExamsMessage("Öğrenci numarası bulunamadı");
                        }
                    } else {
                        showNoExamsMessage("Kullanıcı bilgisi alınamadı");
                    }
                });
    }

    private void loadExamsByStudentNumber(String studentNumber) {
        Log.d("EXAM_RESULTS", "Denemeler yükleniyor: " + studentNumber);

        firestore.collection("ogrenci_karneleri")
                .whereEqualTo("ogrenci_numarasi", studentNumber)
                .get()
                .addOnCompleteListener(task -> {
                    progressBarLoading.setVisibility(View.GONE);

                    if (task.isSuccessful() && task.getResult() != null) {
                        examResults.clear();
                        List<String> examNames = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                StudentResult result = document.toObject(StudentResult.class);
                                examResults.add(result);

                                // Spinner için isim oluştur
                                String examName = createExamDisplayName(result);
                                examNames.add(examName);

                                Log.d("EXAM_RESULTS", "Deneme eklendi: " + examName);
                            } catch (Exception e) {
                                Log.e("EXAM_RESULTS", "Deneme parse hatası: " + e.getMessage());
                            }
                        }

                        if (examResults.isEmpty()) {
                            showNoExamsMessage("Henüz deneme sonucu bulunmuyor");
                        } else {
                            updateSpinner(examNames);
                        }

                        Log.d("EXAM_RESULTS", "Toplam " + examResults.size() + " deneme yüklendi");
                    } else {
                        Log.e("EXAM_RESULTS", "Denemeler yüklenemedi", task.getException());
                        showNoExamsMessage("Denemeler yüklenemedi");
                    }
                });
    }

    private String createExamDisplayName(StudentResult result) {
        StringBuilder name = new StringBuilder();

        // Tarih ekle (long değeri Date'e çevir)
        if (result.getTarih() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            name.append(sdf.format(new Date(result.getTarih())));
        } else {
            name.append("Tarih bilinmiyor");
        }

        // Okul adı varsa ekle (sinav_adi yerine okul kullan)
        if (result.getOkul() != null && !result.getOkul().isEmpty()) {
            name.append(" - ").append(result.getOkul());
        }

        // Eğer çok uzunsa kısalt
        if (name.length() > 50) {
            return name.substring(0, 47) + "...";
        }

        return name.toString();
    }

    private void updateSpinner(List<String> examNames) {
        spinnerAdapter.clear();
        spinnerAdapter.addAll(examNames);
        spinnerAdapter.notifyDataSetChanged();

        // İlk öğeyi seç
        if (!examNames.isEmpty()) {
            spinnerExams.setSelection(0);
        }
    }

    private void displayExamResult(StudentResult result) {
        Log.d("EXAM_DISPLAY", "Deneme gösteriliyor: " + result.getOgrenci_adi());

        // Genel bilgileri göster
        if (result.getTarih() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            textViewExamDate.setText(sdf.format(new Date(result.getTarih())));
        } else {
            textViewExamDate.setText("Tarih bilinmiyor");
        }

        // Okul adını göster (sinav_adi yerine)
        if (result.getOkul() != null && !result.getOkul().isEmpty()) {
            textViewExamName.setText(result.getOkul());
        } else {
            textViewExamName.setText("Okul bilgisi bilinmiyor");
        }

        // Net bilgilerini göster
        displayNetResults(result.getNet_bilgileri());

        // Eksik konuları derse göre gruplayarak göster
        displayWeakTopicsBySubject(result.getEksik_konular(), result.getNet_bilgileri());

        // Sonuç alanını görünür yap
        scrollViewResults.setVisibility(View.VISIBLE);
    }

    private void displayNetResults(Map<String, Map<String, Object>> netBilgileri) {
        if (netBilgileri != null && !netBilgileri.isEmpty()) {
            // Object değerlerini Integer'a dönüştür
            List<Map.Entry<String, Map<String, Integer>>> netList = new ArrayList<>();
            
            for (Map.Entry<String, Map<String, Object>> entry : netBilgileri.entrySet()) {
                String subject = entry.getKey();
                Map<String, Object> objectData = entry.getValue();
                Map<String, Integer> integerData = new HashMap<>();
                
                // Object değerlerini Integer'a çevir
                for (Map.Entry<String, Object> dataEntry : objectData.entrySet()) {
                    String key = dataEntry.getKey();
                    Object value = dataEntry.getValue();
                    
                    if (value instanceof Number) {
                        integerData.put(key, ((Number) value).intValue());
                    } else {
                        integerData.put(key, 0);
                    }
                }
                
                netList.add(new HashMap.SimpleEntry<>(subject, integerData));
            }
            
            netResultAdapter.updateResults(netList);
            
            recyclerViewNetResults.setVisibility(View.VISIBLE);
            textViewNoNetResults.setVisibility(View.GONE);
            
            Log.d("NET_DISPLAY", "Net bilgileri gösteriliyor: " + netBilgileri.size() + " ders");
        } else {
            recyclerViewNetResults.setVisibility(View.GONE);
            textViewNoNetResults.setVisibility(View.VISIBLE);
            
            Log.d("NET_DISPLAY", "Net bilgisi bulunamadı");
        }
    }

    private void displayWeakTopicsBySubject(List<String> eksikKonular, Map<String, Map<String, Object>> netBilgileri) {
        if (eksikKonular != null && !eksikKonular.isEmpty()) {
            // Eksik konuları derse göre grupla
            List<WeakTopicBySubject> groupedTopics = groupTopicsBySubject(eksikKonular, netBilgileri);
            
            weakTopicsAdapter.updateSubjects(groupedTopics);
            
            recyclerViewWeakTopics.setVisibility(View.VISIBLE);
            textViewNoWeakTopics.setVisibility(View.GONE);
            
            Log.d("WEAK_DISPLAY", "Eksik konular derse göre gruplandı: " + groupedTopics.size() + " ders");
        } else {
            recyclerViewWeakTopics.setVisibility(View.GONE);
            textViewNoWeakTopics.setVisibility(View.VISIBLE);
            
            Log.d("WEAK_DISPLAY", "Eksik konu bulunamadı");
        }
    }

    private List<WeakTopicBySubject> groupTopicsBySubject(List<String> eksikKonular, Map<String, Map<String, Object>> netBilgileri) {
        // Derse göre eksik konuları grupla
        Map<String, List<String>> subjectTopics = new HashMap<>();
        
        // Net bilgilerinden mevcut dersleri al
        if (netBilgileri != null) {
            for (String subject : netBilgileri.keySet()) {
                subjectTopics.put(formatSubjectName(subject), new ArrayList<>());
            }
        }
        
        // Eksik konuları derslere dağıt (basit bir algoritma ile)
        for (String topic : eksikKonular) {
            String assignedSubject = assignTopicToSubject(topic);
            
            if (!subjectTopics.containsKey(assignedSubject)) {
                subjectTopics.put(assignedSubject, new ArrayList<>());
            }
            
            subjectTopics.get(assignedSubject).add(topic);
        }
        
        // Boş olmayan dersleri listeye çevir
        List<WeakTopicBySubject> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : subjectTopics.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                result.add(new WeakTopicBySubject(entry.getKey(), entry.getValue()));
            }
        }
        
        return result;
    }

    private String assignTopicToSubject(String topic) {
        String lowerTopic = topic.toLowerCase();
        
        // Konu içeriğine göre ders tayini (basit kelime eşleştirmesi)
        if (lowerTopic.contains("paragraf") || lowerTopic.contains("metin") || 
            lowerTopic.contains("anlam") || lowerTopic.contains("cümle") ||
            lowerTopic.contains("kelime") || lowerTopic.contains("gramer") ||
            lowerTopic.contains("yazım") || lowerTopic.contains("noktalama")) {
            return "Türkçe";
        }
        
        if (lowerTopic.contains("sayı") || lowerTopic.contains("işlem") || 
            lowerTopic.contains("geometri") || lowerTopic.contains("alan") ||
            lowerTopic.contains("çevre") || lowerTopic.contains("dört işlem") ||
            lowerTopic.contains("kesir") || lowerTopic.contains("ondalık")) {
            return "Matematik";
        }
        
        if (lowerTopic.contains("bitki") || lowerTopic.contains("hayvan") || 
            lowerTopic.contains("madde") || lowerTopic.contains("enerji") ||
            lowerTopic.contains("güneş") || lowerTopic.contains("dünya") ||
            lowerTopic.contains("besin") || lowerTopic.contains("sağlık")) {
            return "Fen Bilimleri";
        }
        
        if (lowerTopic.contains("harita") || lowerTopic.contains("tarih") || 
            lowerTopic.contains("coğrafya") || lowerTopic.contains("ülke") ||
            lowerTopic.contains("şehir") || lowerTopic.contains("kültür") ||
            lowerTopic.contains("toplum") || lowerTopic.contains("devlet")) {
            return "Sosyal Bilgiler";
        }
        
        if (lowerTopic.contains("english") || lowerTopic.contains("ingilizce") || 
            lowerTopic.contains("grammar") || lowerTopic.contains("vocabulary")) {
            return "İngilizce";
        }
        
        if (lowerTopic.contains("din") || lowerTopic.contains("ahlak") || 
            lowerTopic.contains("değer") || lowerTopic.contains("ibadet") ||
            lowerTopic.contains("peygamber") || lowerTopic.contains("kuran")) {
            return "Din Kültürü";
        }
        
        // Varsayılan olarak Genel kategorisine ata
        return "Genel";
    }

    private String formatSubjectName(String subject) {
        if (subject == null || subject.isEmpty()) return "Bilinmeyen Ders";
        
        // İlk harfi büyük yap
        return subject.substring(0, 1).toUpperCase() + subject.substring(1).toLowerCase();
    }

    private void showNoExamsMessage(String message) {
        progressBarLoading.setVisibility(View.GONE);
        textViewNoExams.setText(message);
        textViewNoExams.setVisibility(View.VISIBLE);
        scrollViewResults.setVisibility(View.GONE);
    }

    private void hideResultDetails() {
        scrollViewResults.setVisibility(View.GONE);
    }
} 