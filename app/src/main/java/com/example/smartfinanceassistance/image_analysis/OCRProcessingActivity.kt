// OCRProcessingActivity.kt
package com.example.smartfinanceassistance.image_analysis

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.example.smartfinanceassistance.databinding.ActivityOcrProcessingBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.TextRecognition
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class OCRProcessingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOcrProcessingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOcrProcessingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = intent.getStringExtra("image_uri") ?: return
        val bitmap = getBitmapFromUri(Uri.parse(imageUri))
        binding.imageView.setImageBitmap(bitmap)

        runTextRecognition(bitmap)
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val original = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
        return original.copy(Bitmap.Config.ARGB_8888, true)
    }

    private fun runTextRecognition(bitmap: Bitmap) {
        val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val result = visionText.text
                binding.textViewResult.text = "OCR 결과:\n$result"
                analyzeWithGroq(result)
            }
            .addOnFailureListener { e ->
                binding.textViewResult.text = "OCR 실패: ${e.message}"
            }
    }

    private fun analyzeWithGroq(resultText: String) {
        val client = OkHttpClient()
        val mediaType = "application/json".toMediaType()

        val messagesArray = org.json.JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", """
                    다음 텍스트가 금융 사기인지 판단해줘. 금융 사기는 다음과 같은 특징을 가질 수 있어:
                    - 비현실적인 수익이나 혜택 약속 (예: "하루 만에 100% 수익 보장")
                    - 긴급한 행동 유도 (예: "지금 바로 송금해야 함")
                    - 개인정보 요구 (예: 계좌번호, 비밀번호)
                    - 의심스러운 링크나 연락처 제공
                    텍스트가 금융 사기인지 아닌지 명확히 판단하고, 한글로 대답해. 판단 이유를 구체적으로 설명하고, 애매한 경우에는 그 이유도 명시해.
                """)
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", resultText)
            })
        }

        val bodyJson = JSONObject().apply {
            put("model", "llama3-8b-8192")
            put("messages", messagesArray)
        }

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .addHeader("Authorization", "Bearer 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")
            .addHeader("Content-Type", "application/json")
            .post(bodyJson.toString().toRequestBody(mediaType))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    binding.textViewResult.text = "API 호출 실패: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (!response.isSuccessful || body == null) {
                    runOnUiThread {
                        binding.textViewResult.text = "응답 오류: $body"
                    }
                    return
                }

                try {
                    val message = JSONObject(body)
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    runOnUiThread {
                        binding.textViewResult.text = message
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        binding.textViewResult.text = "응답 파싱 오류: ${e.message}"
                    }
                }
            }
        })
    }
}