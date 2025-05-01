# KoripKel — Android-приложение для слепых и слабовидящих

**KoripKel** — это Android-приложение, которое помогает людям с нарушениями зрения ориентироваться в пространстве, получая голосовое описание окружающей обстановки с помощью камеры и искусственного интеллекта.

## 🚀 Возможности

- 📸 Делает фото с камеры
- 🤖 Отправляет изображение в ИИ-модель `git-large-coco` (Hugging Face)
- 🌍 Переводит результат на русский язык (через Google Translate Web API)
- 🔊 Озвучивает описание с помощью `TextToSpeech`

## 🧠 Для чего создано?

Для помощи незрячим и слабовидящим людям:
- узнавать, что находится перед ними
- ориентироваться в новой обстановке
- получать голосовую обратную связь от окружающего мира

## 🛠 Используемые технологии

- Kotlin (Android)
- Hugging Face Inference API
- Google Translate (неофициальный API)
- TextToSpeech (Android SDK)
- OkHttp + Coroutines
- JSON (org.json)

## 📦 Как собрать и запустить

1. Клонируйте репозиторий:
```bash
   git clone https://github.com/your-username/koripkel.git
```
2. Откройте проект в Android Studio Arctic Fox или новее

3. Вставьте ваш Hugging Face API ключ (если другой):
```
val hfToken = "hf_..." // в MainActivity.kt
```
4. Убедитесь, что в AndroidManifest.xml есть разрешения:
```
<uses-permission android:name="android.permission.CAMERA"/>
<uses-permission android:name="android.permission.INTERNET"/>
```
5. Подключите зависимости в build.gradle:
```
implementation 'com.squareup.okhttp3:okhttp:4.12.0'
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4'
```
6. Запустите на устройстве с камерой или эмуляторе (API 24+)

## 📝 Пример использования
- Нажмите "Сделать фото"
- Приложение сделает снимок и отправит его в ИИ
- Получит описание, переведёт и озвучит его
- Вы также можете нажать "Озвучить ещё раз"
