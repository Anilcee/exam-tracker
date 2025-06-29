# 🔒 Firebase Security Rules - ExamTracker

## 📝 **Firestore Database Rules**

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Users collection - Kullanıcılar sadece kendi verilerini okuyabilir
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // AdminEmails collection - Sadece okuma, kimse yazamaz (Admin Console'dan yönetilir)
    match /adminEmails/{email} {
      allow read: if request.auth != null;
      allow write: if false; // Sadece Admin Console'dan yazılabilir
    }
    
    // Exams collection - Herkes okuyabilir, sadece adminler yazabilir
    match /exams/{examId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && isAdmin();
    }
    
    // Ogrenci Karneleri collection - Sadece adminler yazabilir, herkes okuyabilir
    match /ogrenci_karneleri/{karneId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && isAdmin();
    }
    
    // Admin kontrolü fonksiyonu
    function isAdmin() {
      return exists(/databases/$(database)/documents/adminEmails/$(request.auth.token.email));
    }
  }
}
```

---

## 📦 **Firebase Storage Rules**

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    
    // Exams klasörü - Herkes okuyabilir, sadece adminler yazabilir
    match /exams/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && isAdmin();
    }
    
    // Admin kontrolü fonksiyonu
    function isAdmin() {
      return firestore.exists(/databases/(default)/documents/adminEmails/$(request.auth.token.email));
    }
  }
}
```

---

## 🚀 **Firebase Console Kurulum Adımları**

### 1. **Authentication Ayarları**
```
Firebase Console → Authentication → Sign-in method
✅ Email/Password → Enable
```

### 2. **Firestore Database Oluşturma**
```
Firebase Console → Firestore Database → Create database
🔹 Start in test mode (geçici)
🔹 Location: europe-west3 (Frankfurt) öneriyoruz
🔹 Rules'ı yukarıdaki kodla değiştirin
```

### 3. **Storage Ayarları**
```
Firebase Console → Storage → Get started
🔹 Start in test mode (geçici)
🔹 Rules'ı yukarıdaki kodla değiştirin
```

### 4. **İlk Admin Ekleme**
```
Firestore Database → adminEmails (collection) → Add document

Document ID: admin@examtracker.com
Fields:
  - email: "admin@examtracker.com"
  - addedAt: (current timestamp)
```

---

## 🔧 **Test Edilmesi Gerekenler**

### ✅ **Başarılı Senaryolar:**
- Admin emaili ile kayıt olunca admin dashboard açılması
- Normal email ile kayıt olunca student dashboard açılması  
- Admin'in PDF yükleyebilmesi
- Admin'in yeni admin ekleyebilmesi
- Admin'in admin kaldırabilmesi
- Student'ın sınav listesini görebilmesi

### ❌ **Başarısız Olması Gerekenler:**
- Student'ın PDF yüklemeye çalışması
- Yetkisiz kullanıcının admin collection'ına yazması
- Oturum açmamış kullanıcının verilere erişmesi

---

## 💡 **Güvenlik İpuçları**

1. **Production Rules:** Test mode'dan çıkarıp yukarıdaki rules'ı kullanın
2. **Email Validation:** Admin emaillerini dikkatli girin
3. **Regular Backup:** Firestore'ı düzenli yedekleyin
4. **Monitor Usage:** Firebase Usage tab'ından kullanımı takip edin
5. **Version Control:** Rules değişikliklerini git'te takip edin

---

## 🔄 **Rules Güncelleme**

Rules değiştirdikten sonra:
```bash
# Test et
Firebase Console → Rules → Simulator

# Deploy et  
Firebase Console → Rules → Publish
```

---

## 📞 **Sorun Giderme**

**Admin yetkisi almıyor?**
- adminEmails collection'ında email'in doğru yazıldığını kontrol edin
- Büyük/küçük harf duyarlılığına dikkat edin

**PDF yüklenmiyor?**
- Storage rules'ın doğru olduğunu kontrol edin
- Internet bağlantısını kontrol edin

**Giriş yapamıyor?**
- Authentication'ın enable olduğunu kontrol edin
- Email format'ının doğru olduğunu kontrol edin 