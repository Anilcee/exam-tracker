package com.example.examTracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class NetResultAdapter extends RecyclerView.Adapter<NetResultAdapter.NetResultViewHolder> {

    private List<Map.Entry<String, Map<String, Integer>>> netResults;

    public NetResultAdapter(List<Map.Entry<String, Map<String, Integer>>> netResults) {
        this.netResults = netResults;
    }

    @NonNull
    @Override
    public NetResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_net_result, parent, false);
        return new NetResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NetResultViewHolder holder, int position) {
        Map.Entry<String, Map<String, Integer>> entry = netResults.get(position);
        String subject = entry.getKey();
        Map<String, Integer> netData = entry.getValue();

        // Ders adını formatla (ilk harfi büyük)
        String formattedSubject = formatSubjectName(subject);
        holder.textViewSubject.setText(formattedSubject);

        // Net bilgilerini al
        int soruSayisi = netData.getOrDefault("soru_sayisi", 0);
        int dogruSayisi = netData.getOrDefault("dogru_sayisi", 0);
        int yanlisSayisi = netData.getOrDefault("yanlis_sayisi", 0);
        int bosSayisi = netData.getOrDefault("bos_sayisi", 0);
        int netSayisi = netData.getOrDefault("net_sayisi", 0);

        // Bilgileri göster
        holder.textViewSoruSayisi.setText("Soru: " + soruSayisi);
        holder.textViewDogruSayisi.setText("Doğru: " + dogruSayisi);
        holder.textViewYanlisSayisi.setText("Yanlış: " + yanlisSayisi);
        holder.textViewBosSayisi.setText("Boş: " + bosSayisi);
        holder.textViewNetSayisi.setText("Net: " + netSayisi);

        // Net sayısına göre renk ayarla
        if (netSayisi >= soruSayisi * 0.8) {
            // %80 ve üzeri - yeşil
            holder.textViewNetSayisi.setTextColor(0xFF4CAF50);
        } else if (netSayisi >= soruSayisi * 0.6) {
            // %60-79 - turuncu
            holder.textViewNetSayisi.setTextColor(0xFFFF9800);
        } else {
            // %60 altı - kırmızı
            holder.textViewNetSayisi.setTextColor(0xFFF44336);
        }
    }

    @Override
    public int getItemCount() {
        return netResults.size();
    }

    public void updateResults(List<Map.Entry<String, Map<String, Integer>>> newResults) {
        this.netResults = newResults;
        notifyDataSetChanged();
    }

    private String formatSubjectName(String subject) {
        if (subject == null || subject.isEmpty()) return "Bilinmeyen Ders";
        
        // İlk harfi büyük yap
        return subject.substring(0, 1).toUpperCase() + subject.substring(1).toLowerCase();
    }

    static class NetResultViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSubject;
        TextView textViewSoruSayisi;
        TextView textViewDogruSayisi;
        TextView textViewYanlisSayisi;
        TextView textViewBosSayisi;
        TextView textViewNetSayisi;

        public NetResultViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSubject = itemView.findViewById(R.id.textViewSubject);
            textViewSoruSayisi = itemView.findViewById(R.id.textViewSoruSayisi);
            textViewDogruSayisi = itemView.findViewById(R.id.textViewDogruSayisi);
            textViewYanlisSayisi = itemView.findViewById(R.id.textViewYanlisSayisi);
            textViewBosSayisi = itemView.findViewById(R.id.textViewBosSayisi);
            textViewNetSayisi = itemView.findViewById(R.id.textViewNetSayisi);
        }
    }
} 