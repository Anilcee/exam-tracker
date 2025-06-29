package com.example.examTracker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class UploadQuestionFragment extends Fragment {

    private static final String TAG = "UploadQuestionFragment";
    private static final int REQUEST_CODE_PICK_PDF = 103;
    
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    
    private Button buttonSelectExamPDF;
    private Button buttonAnalyzePDF;
    private TextView textViewSelectedFile;
    private ProgressBar progressBar;
    
    private File selectedPdfFile = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "UploadQuestionFragment onCreate() çağrıldı");
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "UploadQuestionFragment onCreateView() çağrıldı");
        View view = inflater.inflate(R.layout.fragment_upload_question, container, false);

        // UI elementlerini bağla
        buttonSelectExamPDF = view.findViewById(R.id.buttonSelectExamPDF);
        buttonAnalyzePDF = view.findViewById(R.id.buttonAnalyzePDF);
        textViewSelectedFile = view.findViewById(R.id.textViewSelectedFile);
        progressBar = view.findViewById(R.id.progressBar);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupListeners();
        
        // Başlangıçta analiz butonu deaktif
        buttonAnalyzePDF.setEnabled(false);
    }

    private void setupListeners() {
        // PDF seç butonu
        buttonSelectExamPDF.setOnClickListener(v -> selectExamPDF());

        // PDF analiz et butonu
        buttonAnalyzePDF.setOnClickListener(v -> analyzePDF());
    }

    private void selectExamPDF() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_PICK_PDF);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_PDF && resultCode == getActivity().RESULT_OK && data != null) {
            Uri uri = data.getData();

            try {
                selectedPdfFile = createFileFromUri(uri);

                // Seçilen dosya bilgisini göster
                String fileName = uri.getLastPathSegment();
                if (fileName == null) fileName = "Seçilen PDF";

                textViewSelectedFile.setText("Seçilen dosya: " + fileName);
                textViewSelectedFile.setVisibility(View.VISIBLE);

                // Analiz butonunu aktif et
                buttonAnalyzePDF.setEnabled(true);

                Toast.makeText(getContext(), "Deneme PDF'i seçildi", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "PDF dosyası okunamadı", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createFileFromUri(Uri uri) throws Exception {
        InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
        File file = new File(getContext().getCacheDir(), "tempExam.pdf");
        FileOutputStream outputStream = new FileOutputStream(file);

        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outputStream.close();

        return file;
    }

    private void analyzePDF() {
        if (selectedPdfFile == null) {
            Toast.makeText(getContext(), "Önce PDF dosyası seçiniz", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        buttonAnalyzePDF.setEnabled(false);
        buttonSelectExamPDF.setEnabled(false);

        // PDF analiz özelliği geçici olarak devre dışı
        Toast.makeText(getContext(), "PDF Soru Analiz Özelliği\n(Geliştiriliyor...)\n\nDeneme PDF'inden sorular çıkarılacak. Geokdeniz reis yapıcak", Toast.LENGTH_LONG).show();
        
        // Form temizle
        progressBar.setVisibility(View.GONE);
        buttonAnalyzePDF.setEnabled(false);
        buttonSelectExamPDF.setEnabled(true);
        textViewSelectedFile.setVisibility(View.GONE);
        selectedPdfFile = null;
    }
} 