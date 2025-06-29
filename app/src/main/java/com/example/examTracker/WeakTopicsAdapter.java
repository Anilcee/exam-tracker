package com.example.examTracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WeakTopicsAdapter extends RecyclerView.Adapter<WeakTopicsAdapter.WeakTopicViewHolder> {

    private List<String> weakTopics;

    public WeakTopicsAdapter(List<String> weakTopics) {
        this.weakTopics = weakTopics;
    }

    @NonNull
    @Override
    public WeakTopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weak_topic, parent, false);
        return new WeakTopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeakTopicViewHolder holder, int position) {
        String topic = weakTopics.get(position);
        holder.textViewTopic.setText("• " + topic);
        
        // Sıra numarası göster
        holder.textViewNumber.setText(String.valueOf(position + 1));
    }

    @Override
    public int getItemCount() {
        return weakTopics.size();
    }

    public void updateTopics(List<String> newTopics) {
        this.weakTopics = newTopics;
        notifyDataSetChanged();
    }

    static class WeakTopicViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNumber;
        TextView textViewTopic;

        public WeakTopicViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNumber = itemView.findViewById(R.id.textViewNumber);
            textViewTopic = itemView.findViewById(R.id.textViewTopic);
        }
    }
} 