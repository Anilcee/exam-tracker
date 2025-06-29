package com.example.examTracker;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class DashboardPagerAdapter extends FragmentStateAdapter {

    public DashboardPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new StudentInfoFragment();
            case 1:
                return new ExamResultsFragment();
            default:
                return new StudentInfoFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public String getTabTitle(int position) {
        switch (position) {
            case 0:
                return "Öğrenci Bilgileri";
            case 1:
                return "Deneme Sonuçları";
            default:
                return "";
        }
    }
} 