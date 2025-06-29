package com.example.examTracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examTracker.models.WeakTopicBySubject;

import java.util.List;

public class WeakTopicsBySubjectAdapter extends RecyclerView.Adapter<WeakTopicsBySubjectAdapter.SubjectViewHolder> {

    private List<WeakTopicBySubject> subjectList;
    private OnTopicClickListener onTopicClickListener;

    public interface OnTopicClickListener {
        void onTopicClick(String subject, String topic);
    }

    public WeakTopicsBySubjectAdapter(List<WeakTopicBySubject> subjectList, OnTopicClickListener listener) {
        this.subjectList = subjectList;
        this.onTopicClickListener = listener;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weak_topics_by_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        WeakTopicBySubject subjectData = subjectList.get(position);
        
        // Ders adını göster
        holder.textViewSubject.setText(subjectData.getSubject());
        
        // Bu dersin eksik konularını gösterecek adapter
        ClickableWeakTopicsAdapter topicsAdapter = new ClickableWeakTopicsAdapter(
                subjectData.getTopics(), 
                topic -> {
                    if (onTopicClickListener != null) {
                        onTopicClickListener.onTopicClick(subjectData.getSubject(), topic);
                    }
                }
        );
        
        holder.recyclerViewTopics.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.recyclerViewTopics.setAdapter(topicsAdapter);
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    public void updateSubjects(List<WeakTopicBySubject> newSubjects) {
        this.subjectList = newSubjects;
        notifyDataSetChanged();
    }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSubject;
        RecyclerView recyclerViewTopics;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSubject = itemView.findViewById(R.id.textViewSubject);
            recyclerViewTopics = itemView.findViewById(R.id.recyclerViewTopics);
        }
    }
} 