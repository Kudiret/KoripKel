package com.example.koripkel

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import org.json.JSONArray
import java.util.*

class MainActivity : Activity() {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var descriptionText: TextView
    private val REQUEST_IMAGE_CAPTURE = 1
//    private val hfToken = "your api"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale("ru")
            }
        }

        descriptionText = findViewById(R.id.descriptionText)

        findViewById<Button>(R.id.captureButton).setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            }
        }

        findViewById<Button>(R.id.speakButton).setOnClickListener {
            val text = descriptionText.text.toString()
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }

        requestPermissions(arrayOf(Manifest.permission.CAMERA), 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val bitmap = data?.extras?.get("data") as? Bitmap
            bitmap?.let {
                processImage(it)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun processImage(bitmap: Bitmap) {
        CoroutineScope(Dispatchers.IO).launch {
            val caption = getCaptionFromHuggingFace(bitmap)
            val translated = translateToRussian(caption)
            withContext(Dispatchers.Main) {
                descriptionText.text = translated
                textToSpeech.speak(translated, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    private fun getCaptionFromHuggingFace(bitmap: Bitmap): String {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val url = "https://api-inference.huggingface.co/models/microsoft/git-large-coco"

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray = stream.toByteArray()
        val requestBody = byteArray.toRequestBody("application/octet-stream".toMediaType())

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $hfToken")
            .post(requestBody)
            .build()

        return try {
            val response = client.newCall(request).execute()
            val responseText = response.body?.string() ?: return "Ошибка: пустой ответ"

            return if (responseText.trim().startsWith("[")) {
                val array = JSONArray(responseText)
                val first = array.optJSONObject(0)
                first?.optString("generated_text") ?: "Описание отсутствует"
            } else {
                "Ответ сервера: ${responseText.take(100)}"
            }

        } catch (e: Exception) {
            "Ошибка описания: ${e.localizedMessage}"
        }
    }



    private fun translateToRussian(text: String): String {
        val client = OkHttpClient()
        val encodedText = URLEncoder.encode(text, "UTF-8")
        val url =
            "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=ru&dt=t&q=$encodedText"

        return try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                val match = Regex("""\[\[\s*"(.+?)"""").find(body ?: "")
                match?.groupValues?.get(1) ?: "Не удалось перевести"
            } else {
                "Ошибка перевода: ${response.code}"
            }
        } catch (e: Exception) {
            "Ошибка перевода: ${e.localizedMessage}"
        }
    }

    override fun onDestroy() {
        textToSpeech.shutdown()
        super.onDestroy()
    }
}
