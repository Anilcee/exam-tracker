package com.example.examTracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdminDashboard";
    private FirebaseAuth mAuth;
    private TextView textViewAdminWelcome;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private AdminPagerAdapter pagerAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Log.d(TAG, "AdminDashboardActivity başlatılıyor...");
        
        mAuth = FirebaseAuth.getInstance();

        // UI elementlerini bağla
        textViewAdminWelcome = findViewById(R.id.textViewAdminWelcome);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // ActionBar'ı ayarla
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Exam Tracker - Admin Paneli");
            getSupportActionBar().show();
        }

        // Fragment yapısını kur
        setupFragments();
        
        Log.d(TAG, "AdminDashboardActivity kurulumu tamamlandı");
    }

    private void setupFragments() {
        // ViewPager2 adapter'ını kur
        pagerAdapter = new AdminPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        
        // TabLayout'u ViewPager2 ile bağla
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(pagerAdapter.getTabTitle(position));
        }).attach();
        
        Log.d(TAG, "Admin Fragment yapısı kuruldu");
    }

    private void logout() {
        // Firebase oturumunu kapat
        mAuth.signOut();

        // Login sayfasına yönlendir
        Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
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
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

