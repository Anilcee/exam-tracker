package com.example.examTracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ClickableWeakTopicsAdapter extends RecyclerView.Adapter<ClickableWeakTopicsAdapter.TopicViewHolder> {

    private List<String> topics;
    private OnTopicClickListener onTopicClickListener;

    public interface OnTopicClickListener {
        void onTopicClick(String topic);
    }

    public ClickableWeakTopicsAdapter(List<String> topics, OnTopicClickListener listener) {
        this.topics = topics;
        this.onTopicClickListener = listener;
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_clickable_weak_topic, parent, false);
        return new TopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        String topic = topics.get(position);
        
        // Kırmızı daire içinde numara
        holder.textViewTopicNumber.setText(String.valueOf(position + 1));
        
        // Konu metnini göster
        holder.textViewTopic.setText(topic);
        
        // Tıklama olayını ayarla
        holder.itemView.setOnClickListener(v -> {
            if (onTopicClickListener != null) {
                onTopicClickListener.onTopicClick(topic);
            }
        });
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    static class TopicViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTopicNumber;
        TextView textViewTopic;

        public TopicViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTopicNumber = itemView.findViewById(R.id.textViewTopicNumber);
            textViewTopic = itemView.findViewById(R.id.textViewTopic);
        }
    }
} 