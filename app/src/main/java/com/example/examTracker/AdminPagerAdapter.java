package com.example.examTracker;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AdminPagerAdapter extends FragmentStateAdapter {
    
    private static final int NUM_TABS = 2;
    
    public AdminPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new UploadReportFragment();
            case 1:
                return new UploadQuestionFragment();
            default:
                return new UploadReportFragment();
        }
    }
    
    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
    
    public String getTabTitle(int position) {
        switch (position) {
            case 0:
                return "📄 Karne Yükle";
            case 1:
                return "❓ Soru Yükle";
            default:
                return "Karne Yükle";
        }
    }
} 