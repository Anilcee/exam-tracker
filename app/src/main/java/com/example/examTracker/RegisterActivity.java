package com.example.examTracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.examTracker.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    
    private EditText editTextFirstName, editTextLastName, editTextStudentNumber, editTextEmail, editTextPassword, editTextConfirmPassword;
    private Button buttonRegister;
    private TextView textViewLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI elementlerini bağla
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextStudentNumber = findViewById(R.id.editTextStudentNumber);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);
        progressBar = findViewById(R.id.progressBar);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Bu sayfayı kapat ve login sayfasına dön
            }
        });
    }

    private void registerUser() {
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String studentNumber = editTextStudentNumber.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Validasyon
        if (firstName.isEmpty()) {
            editTextFirstName.setError("Ad gerekli");
            editTextFirstName.requestFocus();
            return;
        }

        if (lastName.isEmpty()) {
            editTextLastName.setError("Soyad gerekli");
            editTextLastName.requestFocus();
            return;
        }

        if (studentNumber.isEmpty()) {
            editTextStudentNumber.setError("Öğrenci numarası gerekli");
            editTextStudentNumber.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            editTextEmail.setError("E-posta gerekli");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Geçerli bir e-posta adresi girin");
            editTextEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Şifre gerekli");
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Şifre en az 6 karakter olmalı");
            editTextPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Şifreler eşleşmiyor");
            editTextConfirmPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Firebase Authentication ile kullanıcı oluştur
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Firestore'a kullanıcı bilgilerini kaydet
                                saveUserToFirestore(user.getUid(), email, "student", studentNumber, firstName, lastName);
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "Kayıt başarısız: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveUserToFirestore(String uid, String email, String role, String studentNumber, String firstName, String lastName) {
        User user = new User(email, role, uid, studentNumber, firstName, lastName);

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(RegisterActivity.this, "Kayıt başarılı!", Toast.LENGTH_SHORT).show();
                        // Ana sayfaya yönlendir
                        Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "Kullanıcı bilgileri kaydedilemedi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
} 