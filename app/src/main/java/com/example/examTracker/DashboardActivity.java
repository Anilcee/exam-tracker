package com.example.examTracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private TextView textViewWelcome;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private DashboardPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        
        // Firebase instance'larını al
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        
        // Kullanıcı giriş yapmış mı kontrol et
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Kullanıcı giriş yapmamışsa login ekranına yönlendir
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
            return;
        }
        
        // View'ları initialize et
        textViewWelcome = findViewById(R.id.textViewWelcome);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        
        // Hoş geldin mesajını ayarla
        setupWelcomeMessage(currentUser);
        
        // Fragment yapısını kur
        setupFragments();
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupWelcomeMessage(FirebaseUser currentUser) {
        // Önce e-posta ile hoş geldin mesajı göster
        textViewWelcome.setText("Hoş geldiniz, " + currentUser.getEmail());
        
        // Sonra kullanıcı bilgilerini çekip daha kişisel mesaj göster
        firestore.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String firstName = task.getResult().getString("firstName");
                        String lastName = task.getResult().getString("lastName");
                        
                        String welcomeMessage;
                        if (firstName != null && lastName != null && !firstName.isEmpty() && !lastName.isEmpty()) {
                            welcomeMessage = "Hoş geldiniz, " + firstName + " " + lastName;
                        } else if (firstName != null && !firstName.isEmpty()) {
                            welcomeMessage = "Hoş geldiniz, " + firstName;
                        } else {
                            welcomeMessage = "Hoş geldiniz, " + currentUser.getEmail();
                        }
                        textViewWelcome.setText(welcomeMessage);
                        
                        Log.d("DASHBOARD", "Hoş geldin mesajı güncellendi: " + welcomeMessage);
                    } else {
                        Log.e("DASHBOARD", "Kullanıcı bilgileri alınamadı", task.getException());
                    }
                });
    }

    private void setupFragments() {
        // ViewPager2 adapter'ını kur
        pagerAdapter = new DashboardPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        
        // TabLayout'u ViewPager2 ile bağla
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(pagerAdapter.getTabTitle(position));
        }).attach();
        
        Log.d("DASHBOARD", "Fragment yapısı kuruldu");
    }

    private void logout() {
        // Firebase oturumunu kapat
        mAuth.signOut();
        
        // Login sayfasına yönlendir
        Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        
        Toast.makeText(this, "Çıkış yapıldı", Toast.LENGTH_SHORT).show();
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
            logout();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
} 