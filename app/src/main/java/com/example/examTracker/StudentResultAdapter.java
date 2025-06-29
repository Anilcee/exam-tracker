package com.example.examTracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examTracker.models.StudentResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StudentResultAdapter extends RecyclerView.Adapter<StudentResultAdapter.ViewHolder> {

    private List<StudentResult> studentResults;

    public StudentResultAdapter(List<StudentResult> studentResults) {
        this.studentResults = studentResults;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentResult result = studentResults.get(position);
        
        // Tarih formatla
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String formattedDate = sdf.format(new Date(result.getTarih()));
        holder.textViewExamDate.setText("Sınav Tarihi: " + formattedDate);
        
        // Okul adı
        holder.textViewSchool.setText(result.getOkul() != null ? result.getOkul() : "Okul bilgisi yok");
        
        // Net değerleri
        holder.textViewTurkceNet.setText(String.format(Locale.getDefault(), "%.1f", result.getNetBySubject("turkce")));
        holder.textViewMatematikNet.setText(String.format(Locale.getDefault(), "%.1f", result.getNetBySubject("matematik")));
        holder.textViewFenNet.setText(String.format(Locale.getDefault(), "%.1f", result.getNetBySubject("fen")));
        holder.textViewSosyalNet.setText(String.format(Locale.getDefault(), "%.1f", result.getNetBySubject("sosyal")));
        holder.textViewIngilizceNet.setText(String.format(Locale.getDefault(), "%.1f", result.getNetBySubject("ingilizce")));
        holder.textViewDinNet.setText(String.format(Locale.getDefault(), "%.1f", result.getNetBySubject("din")));
        
        // Eksik konular
        if (result.getEksik_konular() != null && !result.getEksik_konular().isEmpty()) {
            StringBuilder eksikKonular = new StringBuilder("Eksik Konular: ");
            for (int i = 0; i < result.getEksik_konular().size(); i++) {
                eksikKonular.append(result.getEksik_konular().get(i));
                if (i < result.getEksik_konular().size() - 1) {
                    eksikKonular.append(", ");
                }
            }
            holder.textViewWeakTopics.setText(eksikKonular.toString());
            holder.textViewWeakTopics.setVisibility(View.VISIBLE);
        } else {
            holder.textViewWeakTopics.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return studentResults.size();
    }

    public void updateResults(List<StudentResult> newResults) {
        this.studentResults = newResults;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewExamDate;
        TextView textViewSchool;
        TextView textViewTurkceNet;
        TextView textViewMatematikNet;
        TextView textViewFenNet;
        TextView textViewSosyalNet;
        TextView textViewIngilizceNet;
        TextView textViewDinNet;
        TextView textViewWeakTopics;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewExamDate = itemView.findViewById(R.id.textViewExamDate);
            textViewSchool = itemView.findViewById(R.id.textViewSchool);
            textViewTurkceNet = itemView.findViewById(R.id.textViewTurkceNet);
            textViewMatematikNet = itemView.findViewById(R.id.textViewMatematikNet);
            textViewFenNet = itemView.findViewById(R.id.textViewFenNet);
            textViewSosyalNet = itemView.findViewById(R.id.textViewSosyalNet);
            textViewIngilizceNet = itemView.findViewById(R.id.textViewIngilizceNet);
            textViewDinNet = itemView.findViewById(R.id.textViewDinNet);
            textViewWeakTopics = itemView.findViewById(R.id.textViewWeakTopics);
        }
    }
} 