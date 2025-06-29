package com.example.examTracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class QuestionActivity extends AppCompatActivity {

    private TextView textViewSubject;
    private TextView textViewTopic;
    private TextView textViewQuestionCounter;
    private TextView textViewQuestion;
    private RadioGroup radioGroupAnswers;
    private RadioButton radioButtonA, radioButtonB, radioButtonC, radioButtonD;
    private Button buttonNext;
    private Button buttonFinish;

    private String subject;
    private String topic;
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;

    // Soru sınıfı
    private static class Question {
        String questionText;
        String[] options;
        int correctAnswer; // 0=A, 1=B, 2=C, 3=D

        public Question(String questionText, String[] options, int correctAnswer) {
            this.questionText = questionText;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }
    }

    // Örnek sorular veritabanı
    private static final Map<String, List<Question>> SAMPLE_QUESTIONS = new HashMap<>();

    static {
        // Türkçe soruları
        List<Question> turkceQuestions = new ArrayList<>();
        turkceQuestions.add(new Question(
            "Aşağıdaki cümlelerin hangisinde özne yoktur?",
            new String[]{"Dün akşam yağmur yağdı.", "Çiçekler soldu.", "Pencereyi aç!", "Ali okula gitti."},
            2
        ));
        turkceQuestions.add(new Question(
            "\"Kitap okumak bilgi edinmenin en etkili yoludur.\" cümlesinde özne hangisidir?",
            new String[]{"Kitap", "Kitap okumak", "Bilgi", "Yol"},
            1
        ));
        turkceQuestions.add(new Question(
            "Aşağıdaki kelimelerden hangisi isim kökenli fiildir?",
            new String[]{"Yazmak", "Okumak", "Temizlemek", "Koşmak"},
            2
        ));
        turkceQuestions.add(new Question(
            "\"Güneş doğudan doğar.\" cümlesinde yüklem hangisidir?",
            new String[]{"Güneş", "Doğudan", "Doğar", "Doğudan doğar"},
            2
        ));
        turkceQuestions.add(new Question(
            "Aşağıdaki cümlelerin hangisinde nesne vardır?",
            new String[]{"Çocuklar oyun oynuyor.", "Kuşlar uçuyor.", "Ali kitap okudu.", "Yağmur yağıyor."},
            2
        ));
        SAMPLE_QUESTIONS.put("türkçe", turkceQuestions);

        // Matematik soruları
        List<Question> matQuestions = new ArrayList<>();
        matQuestions.add(new Question(
            "25 × 4 işleminin sonucu kaçtır?",
            new String[]{"90", "100", "110", "120"},
            1
        ));
        matQuestions.add(new Question(
            "Bir dikdörtgenin uzun kenarı 8 cm, kısa kenarı 5 cm'dir. Alanı kaç cm²'dir?",
            new String[]{"40", "26", "13", "35"},
            0
        ));
        matQuestions.add(new Question(
            "3/4 + 1/2 işleminin sonucu kaçtır?",
            new String[]{"4/6", "5/4", "4/4", "7/8"},
            1
        ));
        matQuestions.add(new Question(
            "Bir sayının 3 katının 5 fazlası 23'tür. Bu sayı kaçtır?",
            new String[]{"6", "8", "5", "7"},
            0
        ));
        matQuestions.add(new Question(
            "360° açının 1/4'ü kaç derecedir?",
            new String[]{"90°", "180°", "45°", "120°"},
            0
        ));
        SAMPLE_QUESTIONS.put("matematik", matQuestions);

        // Fen Bilimleri soruları
        List<Question> fenQuestions = new ArrayList<>();
        fenQuestions.add(new Question(
            "Bitkiler fotosentez yaparken hangi gazı salar?",
            new String[]{"Karbondioksit", "Oksijen", "Azot", "Hidrojen"},
            1
        ));
        fenQuestions.add(new Question(
            "Ses hangi ortamda en hızlı yayılır?",
            new String[]{"Hava", "Su", "Demir", "Boşluk"},
            2
        ));
        fenQuestions.add(new Question(
            "Aşağıdakilerden hangisi madde değişimi örneğidir?",
            new String[]{"Buzun erimesi", "Kağıdın yanması", "Suyun buharlaşması", "Şekerin suda çözünmesi"},
            1
        ));
        fenQuestions.add(new Question(
            "Dünya'nın en yakın yıldızı hangisidir?",
            new String[]{"Ay", "Güneş", "Mars", "Venüs"},
            1
        ));
        fenQuestions.add(new Question(
            "Elektrik akımının birimi nedir?",
            new String[]{"Volt", "Amper", "Watt", "Ohm"},
            1
        ));
        SAMPLE_QUESTIONS.put("fen bilimleri", fenQuestions);

        // Sosyal Bilgiler soruları
        List<Question> sosyalQuestions = new ArrayList<>();
        sosyalQuestions.add(new Question(
            "Türkiye'nin başkenti neresidir?",
            new String[]{"İstanbul", "İzmir", "Ankara", "Bursa"},
            2
        ));
        sosyalQuestions.add(new Question(
            "Aşağıdakilerden hangisi doğal afettir?",
            new String[]{"Savaş", "Deprem", "Çevre kirliliği", "Göç"},
            1
        ));
        sosyalQuestions.add(new Question(
            "Türkiye kaç il'den oluşur?",
            new String[]{"79", "80", "81", "82"},
            2
        ));
        sosyalQuestions.add(new Question(
            "Aşağıdakilerden hangisi kültürel miras örneğidir?",
            new String[]{"Ayasofya", "Boğaziçi Köprüsü", "Çamlıca Kulesi", "Eurasia Tüneli"},
            0
        ));
        sosyalQuestions.add(new Question(
            "Türkiye'nin en uzun nehri hangisidir?",
            new String[]{"Sakarya", "Kızılırmak", "Yeşilırmak", "Seyhan"},
            1
        ));
        SAMPLE_QUESTIONS.put("sosyal bilgiler", sosyalQuestions);

        // İngilizce soruları
        List<Question> ingilizceQuestions = new ArrayList<>();
        ingilizceQuestions.add(new Question(
            "What is the plural form of 'child'?",
            new String[]{"Childs", "Children", "Childes", "Child"},
            1
        ));
        ingilizceQuestions.add(new Question(
            "Which one is correct?",
            new String[]{"I have a book", "I has a book", "I having a book", "I had have a book"},
            0
        ));
        ingilizceQuestions.add(new Question(
            "What time is it? 3:30",
            new String[]{"It's three thirty", "It's three thirteen", "It's half past four", "It's quarter to three"},
            0
        ));
        ingilizceQuestions.add(new Question(
            "Choose the correct sentence:",
            new String[]{"She don't like apples", "She doesn't likes apples", "She doesn't like apples", "She not like apples"},
            2
        ));
        ingilizceQuestions.add(new Question(
            "What is the opposite of 'big'?",
            new String[]{"Large", "Small", "Huge", "Wide"},
            1
        ));
        SAMPLE_QUESTIONS.put("ingilizce", ingilizceQuestions);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        // ActionBar'ı etkinleştir ve geri butonunu göster
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Deneme Soruları");
        }

        // Intent'ten verileri al
        subject = getIntent().getStringExtra("subject");
        topic = getIntent().getStringExtra("topic");

        // View'ları bağla
        textViewSubject = findViewById(R.id.textViewSubject);
        textViewTopic = findViewById(R.id.textViewTopic);
        textViewQuestionCounter = findViewById(R.id.textViewQuestionCounter);
        textViewQuestion = findViewById(R.id.textViewQuestion);
        radioGroupAnswers = findViewById(R.id.radioGroupAnswers);
        radioButtonA = findViewById(R.id.radioButtonA);
        radioButtonB = findViewById(R.id.radioButtonB);
        radioButtonC = findViewById(R.id.radioButtonC);
        radioButtonD = findViewById(R.id.radioButtonD);
        buttonNext = findViewById(R.id.buttonNext);
        buttonFinish = findViewById(R.id.buttonFinish);

        // Başlangıç bilgilerini ayarla
        textViewSubject.setText("Ders: " + subject);
        textViewTopic.setText("Konu: " + topic);

        // Soruları yükle
        loadQuestions();

        // İlk soruyu göster
        showCurrentQuestion();

        // Buton dinleyicileri
        buttonNext.setOnClickListener(v -> nextQuestion());
        buttonFinish.setOnClickListener(v -> finishQuiz());
    }

    private void loadQuestions() {
        String normalizedSubject = subject.toLowerCase().trim();

        // Eşleştirmeleri kontrol et
        if (normalizedSubject.contains("matematik")) {
            normalizedSubject = "matematik";
        } else if (normalizedSubject.contains("fen")) {
            normalizedSubject = "fen bilimleri";
        } else if (normalizedSubject.contains("sosyal")) {
            normalizedSubject = "sosyal bilgiler";
        } else if (normalizedSubject.contains("türkçe")) {
            normalizedSubject = "türkçe";
        } else if (normalizedSubject.contains("ingilizce")) {
            normalizedSubject = "ingilizce";
        }

        List<Question> subjectQuestions = SAMPLE_QUESTIONS.get(normalizedSubject);
        if (subjectQuestions == null || subjectQuestions.isEmpty()) {
            // Varsayılan sorular
            questions = new ArrayList<>();
            questions.add(new Question(
                "Bu konu hakkında hangi bilgi doğrudur?",
                new String[]{"Seçenek A", "Seçenek B", "Seçenek C", "Seçenek D"},
                0
            ));
            questions.add(new Question(
                "Aşağıdakilerden hangisi bu konuyla ilgilidir?",
                new String[]{"Seçenek A", "Seçenek B", "Seçenek C", "Seçenek D"},
                1
            ));
            questions.add(new Question(
                "Bu konuda önemli olan nokta hangisidir?",
                new String[]{"Seçenek A", "Seçenek B", "Seçenek C", "Seçenek D"},
                2
            ));
        } else {
            // Rastgele 3 soru seç
            questions = new ArrayList<>();
            List<Question> availableQuestions = new ArrayList<>(subjectQuestions);
            Random random = new Random();

            for (int i = 0; i < Math.min(3, availableQuestions.size()); i++) {
                int index = random.nextInt(availableQuestions.size());
                questions.add(availableQuestions.remove(index));
            }
        }

        Log.d("QUESTION_ACTIVITY", "Toplam " + questions.size() + " soru yüklendi");
    }

    private void showCurrentQuestion() {
        if (currentQuestionIndex < questions.size()) {
            Question currentQuestion = questions.get(currentQuestionIndex);

            // Soru sayacını güncelle
            textViewQuestionCounter.setText("Soru " + (currentQuestionIndex + 1) + "/" + questions.size());

            // Soruyu göster
            textViewQuestion.setText(currentQuestion.questionText);

            // Seçenekleri göster
            radioButtonA.setText("A) " + currentQuestion.options[0]);
            radioButtonB.setText("B) " + currentQuestion.options[1]);
            radioButtonC.setText("C) " + currentQuestion.options[2]);
            radioButtonD.setText("D) " + currentQuestion.options[3]);

            // Seçimi temizle
            radioGroupAnswers.clearCheck();

            // Buton görünürlüğünü ayarla
            if (currentQuestionIndex == questions.size() - 1) {
                buttonNext.setVisibility(View.GONE);
                buttonFinish.setVisibility(View.VISIBLE);
            } else {
                buttonNext.setVisibility(View.VISIBLE);
                buttonFinish.setVisibility(View.GONE);
            }
        }
    }

    private void nextQuestion() {
        // Cevabı kontrol et
        checkAnswer();

        // Sonraki soruya geç
        currentQuestionIndex++;
        showCurrentQuestion();
    }

    private void checkAnswer() {
        int selectedId = radioGroupAnswers.getCheckedRadioButtonId();
        if (selectedId != -1) {
            int selectedAnswer = -1;

            if (selectedId == R.id.radioButtonA) selectedAnswer = 0;
            else if (selectedId == R.id.radioButtonB) selectedAnswer = 1;
            else if (selectedId == R.id.radioButtonC) selectedAnswer = 2;
            else if (selectedId == R.id.radioButtonD) selectedAnswer = 3;

            Question currentQuestion = questions.get(currentQuestionIndex);
            if (selectedAnswer == currentQuestion.correctAnswer) {
                correctAnswers++;
                Toast.makeText(this, "Doğru! ✓", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Yanlış! Doğru cevap: " + 
                    (char)('A' + currentQuestion.correctAnswer), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Lütfen bir seçenek işaretleyin", Toast.LENGTH_SHORT).show();
        }
    }

    private void finishQuiz() {
        // Son sorunun cevabını kontrol et
        checkAnswer();

        // Sonuçları göster
        int percentage = (int) ((double) correctAnswers / questions.size() * 100);
        
        String message = "Sonuçlarınız:\n\n" +
                "Doğru: " + correctAnswers + "/" + questions.size() + "\n" +
                "Başarı: %" + percentage + "\n\n";
        
        if (percentage >= 70) {
            message += "Tebrikler! Bu konuyu iyi biliyorsunuz. 🎉";
        } else if (percentage >= 50) {
            message += "Fena değil! Biraz daha çalışarak geliştirebilirsiniz. 📚";
        } else {
            message += "Bu konuyu tekrar etmeniz faydalı olacak. 💪";
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Test Tamamlandı")
                .setMessage(message)
                .setPositiveButton("Tamam", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 