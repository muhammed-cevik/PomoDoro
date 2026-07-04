# PomoDoro

Minimalist Pomodoro zamanlayıcı. Kotlin + Jetpack Compose.

## Özellikler

- Çalışma / mola sürelerini her seferinde değiştirebilme
- Geçmiş oturum kayıtları (DataStore ile kalıcı)
- Sade, emojisiz, siyah/beyaz arayüz (Mplayer ile aynı tema dili)
- Saat/kronometre temalı uygulama ikonu

## Build

```
./gradlew assembleDebug
```

GitHub Actions ile her push'ta otomatik debug APK üretilir (`Actions` sekmesi → Artifacts).
Release imzalı APK için repo secrets'a `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` eklenmelidir; eklenmezse sadece imzasız debug APK üretilir.

## Paket adı

`com.tdev.pomodoro`
