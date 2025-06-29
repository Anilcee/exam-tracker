package com.example.examTracker;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StudentInfoFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    
    private TextView textViewStudentName;
    private TextView textViewStudentNumber;
    private TextView textViewStudentEmail;
    private TextView textViewStudentSchool;
    private TextView textViewTotalExams;
    private TextView textViewLastExamDate;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_info, container, false);
        
        // View'ları bağla
        textViewStudentName = view.findViewById(R.id.textViewStudentName);
        textViewStudentNumber = view.findViewById(R.id.textViewStudentNumber);
        textViewStudentEmail = view.findViewById(R.id.textViewStudentEmail);
        textViewStudentSchool = view.findViewById(R.id.textViewStudentSchool);
        textViewTotalExams = view.findViewById(R.id.textViewTotalExams);
        textViewLastExamDate = view.findViewById(R.id.textViewLastExamDate);
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Kullanıcı bilgilerini yükle
        loadStudentInfo();
        loadStudentStatistics();
    }

    private void loadStudentInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        Log.d("STUDENT_INFO", "Öğrenci bilgileri yükleniyor: " + currentUser.getUid());

        firestore.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Kullanıcı bilgilerini al ve görüntüle
                        String firstName = task.getResult().getString("firstName");
                        String lastName = task.getResult().getString("lastName");
                        String studentNumber = task.getResult().getString("studentNumber");

                        // Ad soyad
                        String fullName = "";
                        if (firstName != null && !firstName.isEmpty()) {
                            fullName += firstName;
                        }
                        if (lastName != null && !lastName.isEmpty()) {
                            if (!fullName.isEmpty()) fullName += " ";
                            fullName += lastName;
                        }
                        if (fullName.isEmpty()) {
                            fullName = "Bilgi bulunamadı";
                        }
                        textViewStudentName.setText(fullName);

                        // Öğrenci numarası
                        if (studentNumber != null && !studentNumber.isEmpty()) {
                            textViewStudentNumber.setText(studentNumber);
                        } else {
                            textViewStudentNumber.setText("Bilgi bulunamadı");
                        }

                        // E-posta
                        if (currentUser.getEmail() != null) {
                            textViewStudentEmail.setText(currentUser.getEmail());
                        } else {
                            textViewStudentEmail.setText("Bilgi bulunamadı");
                        }

                        // Okul - Kayıt sırasında alınmadığı için varsayılan mesaj
                        textViewStudentSchool.setText("Okul bilgisi henüz eklenmemiş");

                        Log.d("STUDENT_INFO", "Öğrenci bilgileri başarıyla yüklendi");
                    } else {
                        Log.e("STUDENT_INFO", "Öğrenci bilgileri alınamadı", task.getException());
                        textViewStudentName.setText("Hata: Bilgiler alınamadı");
                        textViewStudentNumber.setText("Hata: Bilgiler alınamadı");
                        textViewStudentEmail.setText("Hata: Bilgiler alınamadı");
                        textViewStudentSchool.setText("Hata: Bilgiler alınamadı");
                    }
                });
    }

    private void loadStudentStatistics() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        // Önce öğrenci numarasını al
        firestore.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String studentNumber = task.getResult().getString("studentNumber");
                        if (studentNumber != null && !studentNumber.isEmpty()) {
                            loadExamStatistics(studentNumber);
                        } else {
                            textViewTotalExams.setText("0");
                            textViewLastExamDate.setText("Öğrenci numarası bulunamadı");
                        }
                    } else {
                        textViewTotalExams.setText("0");
                        textViewLastExamDate.setText("Kullanıcı bilgisi alınamadı");
                    }
                });
    }

    private void loadExamStatistics(String studentNumber) {
        Log.d("STUDENT_STATS", "İstatistikler yükleniyor: " + studentNumber);

        firestore.collection("ogrenci_karneleri")
                .whereEqualTo("ogrenci_numarasi", studentNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        int totalExams = task.getResult().size();
                        textViewTotalExams.setText(String.valueOf(totalExams));

                        // Son deneme tarihini bul
                        Date lastDate = null;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                com.google.firebase.Timestamp timestamp = document.getTimestamp("tarih");
                                if (timestamp != null) {
                                    Date examDate = timestamp.toDate();
                                    if (lastDate == null || examDate.after(lastDate)) {
                                        lastDate = examDate;
                                    }
                                }
                            } catch (Exception e) {
                                Log.e("STUDENT_STATS", "Tarih parse hatası: " + e.getMessage());
                            }
                        }

                        // Son deneme tarihini formatlı göster
                        if (lastDate != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                            textViewLastExamDate.setText(sdf.format(lastDate));
                        } else {
                            textViewLastExamDate.setText("Henüz deneme yok");
                        }

                        Log.d("STUDENT_STATS", "İstatistikler başarıyla yüklendi. Toplam: " + totalExams);
                    } else {
                        Log.e("STUDENT_STATS", "İstatistikler alınamadı", task.getException());
                        textViewTotalExams.setText("0");
                        textViewLastExamDate.setText("Hata: Veriler alınamadı");
                    }
                });
    }
} 