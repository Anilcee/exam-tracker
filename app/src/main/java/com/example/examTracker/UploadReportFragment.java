package com.example.examTracker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadReportFragment extends Fragment {

    private static final String TAG = "UploadReportFragment";
    private static final int REQUEST_CODE_PICK_PDF = 101;
    private FirebaseFirestore firestore;
    
    private Button buttonSelectPDF;
    private Button buttonUploadPDF;
    private TextView textViewSelectedFile;
    private TextView textViewAnalyzing;
    private ProgressBar progressBar;
    private File selectedPdfFile = null;

    // Format 2 Pages için öğrenci verileri önbelleği
    private Map<String, StudentDataCache> studentCache = new HashMap<>();

    // PDF Formatlarını tanımlayan enum
    private enum PdfFormat {
        FORMAT_2_PAGES, // ALİ GİRAY KURT tipi (2 sayfa tek öğrenci)
        FORMAT_1_PAGE,  // GÜLERNAZ SUZUK tipi (1 sayfa tek öğrenci)
        GENERAL         // Genel, tanımsız format
    }

    // Öğrenci bilgisi sınıfı
    private static class StudentInfo {
        String name = "";
        String number = "";
        String school = "";

        @Override
        public String toString() {
            return "Ad: " + name + ", Numara: " + number + ", Okul: " + school;
        }
    }

    // Format 2 Pages için öğrenci verilerini önbellekte tutan sınıf
    private static class StudentDataCache {
        StudentInfo studentInfo;
        String[] page1Lines;
        String[] page2Lines;
        int pageCount; // Kaç sayfasının işlendiği
    }

    // Konu parse sonucunu tutan yardımcı sınıf
    private static class TopicResult {
        String topicText;
        boolean isWeak; // Konunun zayıf konu olup olmadığı

        public TopicResult(String topicText, boolean isWeak) {
            this.topicText = topicText;
            this.isWeak = isWeak;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "UploadReportFragment onCreate() çağrıldı");
        
        // PDFBox'ı başlat
        PDFBoxResourceLoader.init(getContext().getApplicationContext());
        firestore = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "UploadReportFragment onCreateView() çağrıldı");
        View view = inflater.inflate(R.layout.fragment_upload_report, container, false);

        // UI elementlerini bağla
        buttonSelectPDF = view.findViewById(R.id.buttonSelectPDF);
        buttonUploadPDF = view.findViewById(R.id.buttonUploadPDF);
        textViewSelectedFile = view.findViewById(R.id.textViewSelectedFile);
        textViewAnalyzing = view.findViewById(R.id.textViewAnalyzing);
        progressBar = view.findViewById(R.id.progressBar);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "UploadReportFragment onViewCreated() çağrıldı");

        // PDF seç butonu - sadece dosya seçer
        buttonSelectPDF.setOnClickListener(v -> pickPdfFile());

        // PDF yükle butonu - seçilen dosyayı analiz eder
        buttonUploadPDF.setOnClickListener(v -> {
            if (selectedPdfFile != null) {
                uploadAndAnalyzePdf();
            } else {
                Toast.makeText(getContext(), "Önce bir PDF dosyası seçiniz", Toast.LENGTH_SHORT).show();
            }
        });

        // Başlangıçta yükle butonu deaktif
        buttonUploadPDF.setEnabled(false);
    }

    private void pickPdfFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_PICK_PDF);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_PDF && resultCode == getActivity().RESULT_OK && data != null) {
            Uri uri = data.getData();

            try {
                selectedPdfFile = createFileFromUri(uri);

                // Seçilen dosya bilgisini göster
                String fileName = uri.getLastPathSegment();
                if (fileName == null) fileName = "Seçilen PDF";

                textViewSelectedFile.setText("Seçilen dosya: " + fileName);
                textViewSelectedFile.setVisibility(View.VISIBLE);

                // Yükle butonunu aktif et
                buttonUploadPDF.setEnabled(true);

                Toast.makeText(getContext(), "PDF dosyası seçildi", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "PDF dosyası okunamadı", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createFileFromUri(Uri uri) throws Exception {
        InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
        File file = new File(getContext().getCacheDir(), "tempKarne.pdf");
        FileOutputStream outputStream = new FileOutputStream(file);

        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outputStream.close();

        return file;
    }

    private void uploadAndAnalyzePdf() {
        if (selectedPdfFile == null) {
            Toast.makeText(getContext(), "Dosya seçilmedi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Progress bar'ı göster
        progressBar.setVisibility(View.VISIBLE);
        textViewAnalyzing.setVisibility(View.VISIBLE);
        buttonUploadPDF.setEnabled(false);
        buttonSelectPDF.setEnabled(false);

        // Background thread'de analiz yap
        new Thread(() -> {
            try {
                // Önce dosyanın hash değerini hesapla
                String fileHash = calculateFileHash(selectedPdfFile);
                Log.d(TAG, "PDF Hash: " + fileHash);
                
                // Veritabanında aynı hash'e sahip dosya var mı kontrol et
                checkDuplicateAndProceed(fileHash);
                
            } catch (Exception e) {
                e.printStackTrace();
                
                // UI thread'de hata mesajını göster
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "PDF hash hesaplanamadı", Toast.LENGTH_SHORT).show();
                        
                        // UI'yi eski haline getir
                        progressBar.setVisibility(View.GONE);
                        textViewAnalyzing.setVisibility(View.GONE);
                        buttonUploadPDF.setEnabled(true);
                        buttonSelectPDF.setEnabled(true);
                    });
                }
            }
        }).start();
    }

    // PDF dosyasının MD5 hash değerini hesaplar
    private String calculateFileHash(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        FileInputStream fis = new FileInputStream(file);
        
        byte[] buffer = new byte[8192];
        int bytesRead;
        
        while ((bytesRead = fis.read(buffer)) != -1) {
            md.update(buffer, 0, bytesRead);
        }
        
        fis.close();
        
        byte[] hashBytes = md.digest();
        StringBuilder sb = new StringBuilder();
        
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        
        return sb.toString();
    }

    // Duplicate kontrolü yapar ve sonucuna göre işlemi devam ettirir
    private void checkDuplicateAndProceed(String fileHash) {
        firestore.collection("ogrenci_karneleri")
                .whereEqualTo("file_hash", fileHash)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Duplicate bulundu
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    progressBar.setVisibility(View.GONE);
                                    textViewAnalyzing.setVisibility(View.GONE);
                                    buttonUploadPDF.setEnabled(true);
                                    buttonSelectPDF.setEnabled(true);
                                    
                                    Toast.makeText(getContext(), 
                                        "Bu deneme raporu daha önce yüklenmiş! (" + querySnapshot.size() + " kayıt bulundu)", 
                                        Toast.LENGTH_LONG).show();
                                });
                            }
                        } else {
                            // Duplicate bulunamadı, analizi başlat
                            try {
                                extractTextWithPdfBox(selectedPdfFile, fileHash);
                            } catch (Exception e) {
                                e.printStackTrace();
                                
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        Toast.makeText(getContext(), "PDF analiz edilemedi", Toast.LENGTH_SHORT).show();
                                        
                                        progressBar.setVisibility(View.GONE);
                                        textViewAnalyzing.setVisibility(View.GONE);
                                        buttonUploadPDF.setEnabled(true);
                                        buttonSelectPDF.setEnabled(true);
                                    });
                                }
                            }
                        }
                    } else {
                        // Firestore sorgu hatası
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                textViewAnalyzing.setVisibility(View.GONE);
                                buttonUploadPDF.setEnabled(true);
                                buttonSelectPDF.setEnabled(true);
                                
                                Toast.makeText(getContext(), 
                                    "Veritabanı kontrolü yapılamadı: " + task.getException().getMessage(), 
                                    Toast.LENGTH_LONG).show();
                            });
                        }
                    }
                });
    }

    private void extractTextWithPdfBox(File file, String fileHash) {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true); // yazı sırasını korur

            int totalPages = document.getNumberOfPages();

            // Her sayfayı ayrı ayrı işle
            for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                stripper.setStartPage(pageNum);
                stripper.setEndPage(pageNum);

                String pageText = stripper.getText(document);
                Log.d("PDF_PAGE_TEXT", "Sayfa " + pageNum + ": " + pageText);

                analyzeKarneText(pageText, pageNum, fileHash);
            }

            // İşlem tamamlandı - UI'yi UI thread'de güncelle
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    textViewAnalyzing.setVisibility(View.GONE);
                    buttonUploadPDF.setEnabled(false);
                    buttonSelectPDF.setEnabled(true);

                    // Seçilen dosya bilgisini temizle
                    textViewSelectedFile.setVisibility(View.GONE);
                    selectedPdfFile = null;

                    Toast.makeText(getContext(), totalPages + " sayfa başarıyla işlendi ve kaydedildi", Toast.LENGTH_LONG).show();
                });
            }

        } catch (Exception e) {
            e.printStackTrace();

            // Hata durumunda UI'yi UI thread'de güncelle
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    textViewAnalyzing.setVisibility(View.GONE);
                    buttonUploadPDF.setEnabled(true);
                    buttonSelectPDF.setEnabled(true);

                    Toast.makeText(getContext(), "PDF analiz edilemedi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }
    }

    private void analyzeKarneText(String fullText, int pageNumber, String fileHash) {
        String[] lines = fullText.split("\n");
        List<String> eksikKonular = new ArrayList<>();

        // Öğrenci bilgileri için değişkenler
        String ogrenciAdi = "";
        String ogrenciNumarasi = "";
        String okul = "";

        // Her satırı debug et
        Log.d("PAGE_LINES", "Sayfa " + pageNumber + " satır sayısı: " + lines.length);
        for (int i = 0; i < Math.min(lines.length, 30); i++) {
            Log.d("PAGE_LINES", "Satır " + i + ": '" + lines[i].trim() + "'");
        }

        // Öğrenci bilgilerini çıkar
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            // Okul bilgisini bul
            if ((line.contains("OKULU") || line.contains("ORTAOKUL")) && okul.isEmpty()) {
                // Okul ismini temizle - ORTAOKULU veya OKULU'ndan sonrasını kes
                String tempOkul = line;
                
                if (tempOkul.contains("ORTAOKULU")) {
                    int index = tempOkul.indexOf("ORTAOKULU");
                    tempOkul = tempOkul.substring(0, index + "ORTAOKULU".length());
                } else if (tempOkul.contains("OKULU")) {
                    int index = tempOkul.indexOf("OKULU");
                    tempOkul = tempOkul.substring(0, index + "OKULU".length());
                }
                
                okul = tempOkul.trim();
                Log.d("STUDENT_PARSE", "Okul bulundu: '" + okul + "' satır " + i);
            }
        }

        // Sadece 4. satırı parse et (index 4)
        if (lines.length > 4) {
            String studentLine = lines[4].trim();
            Log.d("STUDENT_PARSE", "4. satır parse ediliyor: '" + studentLine + "'");

            // Sayı görene kadar her şey isim
            String[] parts = studentLine.split("\\s+");
            StringBuilder nameBuilder = new StringBuilder();
            int numaraIndex = -1;

            for (int j = 0; j < parts.length; j++) {
                if (parts[j].matches("\\d+")) {
                    numaraIndex = j;
                    break;
                }
                nameBuilder.append(parts[j]).append(" ");
            }

            if (numaraIndex != -1) {
                ogrenciAdi = nameBuilder.toString().trim();
                ogrenciNumarasi = parts[numaraIndex];

                Log.d("STUDENT_PARSE", "4. satırdan parse edildi - Ad: '" + ogrenciAdi + "', Numara: '" + ogrenciNumarasi + "'");
            }
        }

        // Net bilgilerini parse et
        Map<String, Map<String, Object>> netBilgileri = new HashMap<>();

        // Türkçe - 19. satır (index 19)
        if (lines.length > 19) {
            Map<String, Object> turkceNet = parseNetBilgisi(lines[19], "Türkçe");
            if (turkceNet != null) {
                netBilgileri.put("turkce", turkceNet);
            }
        }

        // Sosyal - 21. satır (index 21)
        if (lines.length > 21) {
            Map<String, Object> sosyalNet = parseNetBilgisi(lines[21], "Sosyal");
            if (sosyalNet != null) {
                netBilgileri.put("sosyal", sosyalNet);
            }
        }

        // Din - 23. satır (index 23)
        if (lines.length > 23) {
            Map<String, Object> dinNet = parseNetBilgisi(lines[23], "Din");
            if (dinNet != null) {
                netBilgileri.put("din", dinNet);
            }
        }

        // İngilizce - 25. satır (index 25)
        if (lines.length > 25) {
            Map<String, Object> ingilizceNet = parseNetBilgisi(lines[25], "İngilizce");
            if (ingilizceNet != null) {
                netBilgileri.put("ingilizce", ingilizceNet);
            }
        }

        // Matematik - 27. satır (index 27)
        if (lines.length > 27) {
            Map<String, Object> matematikNet = parseNetBilgisi(lines[27], "Matematik");
            if (matematikNet != null) {
                netBilgileri.put("matematik", matematikNet);
            }
        }

        // Fen - 29. satır (index 29)
        if (lines.length > 29) {
            Map<String, Object> fenNet = parseNetBilgisi(lines[29], "Fen");
            if (fenNet != null) {
                netBilgileri.put("fen", fenNet);
            }
        }

        // Eksik konuları analiz et
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            // Bu desen: en sonda 4 sayı (S D Y B) varsa eşleşir
            if (line.matches(".*\\b\\d+\\s+\\d+\\s+\\d+\\s+\\d+\\b.*")) {
                try {
                    // Sondaki 4 sayıyı al
                    String[] parts = line.split("\\s+");
                    int len = parts.length;

                    // Son 4 eleman
                    int s = Integer.parseInt(parts[len - 4]);
                    int d = Integer.parseInt(parts[len - 3]);
                    int y = Integer.parseInt(parts[len - 2]);
                    int b = Integer.parseInt(parts[len - 1]);

                    // Konuyu belirlemek için son 4 sayıyı çıkart
                    StringBuilder konuBuilder = new StringBuilder();
                    for (int j = 0; j < len - 4; j++) {
                        String word = parts[j];
                        // Sadece rakam olan kısımları atla, ayrıca öğrenci bilgilerini de atla
                        if (!word.matches("\\d+") && !word.matches("[0-9][A-ZÇĞİÖŞÜ]") && 
                            !word.equals(ogrenciAdi.split(" ")[0]) && 
                            (ogrenciAdi.split(" ").length < 2 || !word.equals(ogrenciAdi.split(" ")[1]))) {
                            konuBuilder.append(word).append(" ");
                        }
                    }

                    String rawKonu = konuBuilder.toString().trim();
                    
                    // Konu temizleme işlemi
                    String temizKonu = cleanTopicText(rawKonu);

                    // B (başarı) değeri 50'den düşükse ve temizlenmiş konu boş değilse eksik konu olarak ekle
                    if (b < 50 && !temizKonu.isEmpty()) {
                        eksikKonular.add(temizKonu);
                        Log.d("ANALYZE_WEAK", "Sayfa " + pageNumber + " - Ham konu: '" + rawKonu + "' -> Temiz konu: '" + temizKonu + "' (Başarı: " + b + "%)");
                    }

                } catch (Exception e) {
                    Log.e("PARSE_ERROR", "Satır ayrıştırılamadı: " + line);
                }
            }
        }

        // Çıkarılan bilgileri log'la
        Log.d("STUDENT_INFO", "Sayfa " + pageNumber + " - Öğrenci Adı: '" + ogrenciAdi + "'");
        Log.d("STUDENT_INFO", "Sayfa " + pageNumber + " - Öğrenci Numarası: '" + ogrenciNumarasi + "'");
        Log.d("STUDENT_INFO", "Sayfa " + pageNumber + " - Okul: '" + okul + "'");

        // Firestore'a her öğrenci için ayrı kayıt
        Map<String, Object> data = new HashMap<>();
        data.put("ogrenci_adi", ogrenciAdi);
        data.put("ogrenci_numarasi", ogrenciNumarasi);
        data.put("okul", okul);
        data.put("eksik_konular", eksikKonular);
        data.put("sayfa_numarasi", pageNumber);
        data.put("tarih", System.currentTimeMillis());
        data.put("net_bilgileri", netBilgileri);
        data.put("file_hash", fileHash);

        firestore.collection("ogrenci_karneleri")
                .add(data)
                .addOnSuccessListener(doc -> {
                    Log.d("Firestore", "Sayfa " + pageNumber + " kaydedildi: " + doc.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Sayfa " + pageNumber + " kaydetme hatası", e);
                });
    }

    private String cleanTopicText(String rawTopic) {
        if (rawTopic == null || rawTopic.trim().isEmpty()) {
            return "";
        }
        
        String cleaned = rawTopic.trim();
        
        // Filtrelenecek ders adları listesi
        String[] dersAdlari = {
            "Türkçe", "Matematik", "Fen", "Sosyal", "İngilizce", "Din", 
            "LGS", "Başarı", "Sınıf", "Kurum", "Genel",
            "Din K.ve A.B.", "TÜRKÇE", "MATEMATİK", "FEN", "SOSYAL", "İNGİLİZCE",
            "Cevap", "Anh", "Anahtarı", "CEVAP", "ANH", "ANAHTARI", "K.ve A.B.",
            "Anh.", "Cevap Anh.", "CEVAP ANH."
        };
        
        // Ham metni kelimelere ayır
        String[] words = cleaned.split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            // Boş kelimeyi atla
            if (word.trim().isEmpty()) continue;
            
            // Sadece sayı olan kelimeleri atla (123, 374,166 gibi) - virgül ve noktalı sayıları da dahil et
            if (word.matches("\\d+") || word.matches("\\d+[,.]\\d+")) {
                continue;
            }
            
            // Cevap anahtarı harflerini atla - büyük/küçük harf karışımı da dahil
            // En az 4 karakter ve sadece A-E harfleri (büyük/küçük harf)
            if (word.matches("[A-Ea-e]{4,}")) {
                continue;
            }
            
            // Karışık cevap anahtarı pattern'leri - örn: DACBCbBAdCDABBcCBAAD
            if (word.length() > 8 && word.matches("[A-Ea-e]+")) {
                continue;
            }
            
            // Sadece noktalama işareti olan kelimeleri atla
            if (word.matches("[.,;:!?-]+")) {
                continue;
            }
            
            // Ders adlarını kontrol et - tam eşleşme ve içerme kontrolü
            boolean isDersAdi = false;
            for (String ders : dersAdlari) {
                // Tam eşleşme kontrolü
                if (word.equalsIgnoreCase(ders)) {
                    isDersAdi = true;
                    break;
                }
                // İçerme kontrolü - ama çok kısa kelimelerde hatalı eşleşme olmasın
                if (ders.length() > 3 && word.toLowerCase().contains(ders.toLowerCase())) {
                    isDersAdi = true;
                    break;
                }
            }
            
            if (isDersAdi) {
                continue;
            }
            
            // Çok kısa kelimeleri atla (2 karakterden az)
            if (word.length() < 2) {
                continue;
            }
            
            // Sadece noktalama içeren kelimeleri atla
            if (word.matches(".*[.,;:!?-].*") && word.replaceAll("[.,;:!?-]", "").length() < 2) {
                continue;
            }
            
            // Sayı-harf karışımlarını atla (5A, 10B gibi)
            if (word.matches("\\d+[A-Za-z]") || word.matches("[A-Za-z]\\d+")) {
                continue;
            }
            
            // Geçerli kelimeyi ekle
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(word);
        }
        
        String finalResult = result.toString().trim();
        
        // Sonuçta sadece noktalama varsa boş döndür
        if (finalResult.matches("[.,;:!?\\s-]*")) {
            return "";
        }
        
        // Çok kısa sonuçları (3 karakterden az) atla
        if (finalResult.length() < 3) {
            return "";
        }
        
        Log.d("TOPIC_CLEAN", "Ham: '" + rawTopic + "' -> Temiz: '" + finalResult + "'");
        return finalResult;
    }

    private Map<String, Object> parseNetBilgisi(String line, String ders) {
        try {
            Log.d("NET_PARSE", ders + " satırı: '" + line + "'");
            String[] parts = line.split("\\s+");

            if (parts.length >= 5) {
                // İlk sayıyı bul (soru sayısı)
                int sayiIndex = -1;
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].matches("\\d+")) {
                        sayiIndex = i;
                        break;
                    }
                }

                if (sayiIndex != -1 && sayiIndex + 4 < parts.length) {
                    int soruSayisi = Integer.parseInt(parts[sayiIndex]);
                    int dogruSayisi = Integer.parseInt(parts[sayiIndex + 1]);
                    int yanlisSayisi = Integer.parseInt(parts[sayiIndex + 2]);

                    // Net sayısı decimal olabilir (20,00 gibi)
                    String netStr = parts[sayiIndex + 3];
                    double netSayisi = Double.parseDouble(netStr.replace(",", "."));

                    Map<String, Object> netBilgisi = new HashMap<>();
                    netBilgisi.put("ders_adi", ders);
                    netBilgisi.put("soru_sayisi", soruSayisi);
                    netBilgisi.put("dogru_sayisi", dogruSayisi);
                    netBilgisi.put("yanlis_sayisi", yanlisSayisi);
                    netBilgisi.put("net_sayisi", netSayisi);

                    Log.d("NET_PARSE", ders + " - Soru: " + soruSayisi + ", Doğru: " + dogruSayisi + ", Yanlış: " + yanlisSayisi + ", Net: " + netSayisi);
                    return netBilgisi;
                }
            }
        } catch (Exception e) {
            Log.e("NET_PARSE", ders + " parse hatası: " + e.getMessage());
        }
        return null;
    }
} 