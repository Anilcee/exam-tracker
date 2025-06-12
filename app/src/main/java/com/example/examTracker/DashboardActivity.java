package com.example.examTracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView textViewWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        
        // Firebase Auth instance'ını al
        mAuth = FirebaseAuth.getInstance();
        
        // Kullanıcı giriş yapmış mı kontrol et
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Kullanıcı giriş yapmamışsa login ekranına yönlendir
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
            return;
        }
        
        textViewWelcome = findViewById(R.id.textViewWelcome);
        textViewWelcome.setText("Hoş geldiniz, " + currentUser.getEmail());
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout) {
            // Çıkış yap
            mAuth.signOut();
            Toast.makeText(this, "Çıkış yapıldı", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
} 