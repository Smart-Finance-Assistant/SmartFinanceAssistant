// OCRProcessingActivity.kt
package com.example.smartfinanceassistance.image_analysis

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.example.smartfinanceassistance.MainActivity
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
    private var currentImageUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOcrProcessingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = intent.getStringExtra("image_uri") ?: return
        currentImageUri = imageUri

        val bitmap = getBitmapFromUri(Uri.parse(imageUri))
        binding.imageView.setImageBitmap(bitmap)

        // 🔧 버튼 클릭 리스너 추가
        setupButtonListeners()

        runTextRecognition(bitmap)
    }

    // 🆕 버튼 이벤트 설정
    private fun setupButtonListeners() {
        // 다시 분석 버튼
        binding.btnRetry.setOnClickListener {
            // 갤러리 선택 화면으로 돌아가기
            val intent = Intent(this, GallerySelectActivity::class.java)
            startActivity(intent)
            finish() // 현재 액티비티 종료
        }

        // 홈으로 버튼
        binding.btnHome.setOnClickListener {
            // 메인 액티비티로 이동
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // 현재 액티비티 종료
        }
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
        // 🔄 분석 시작 시 UI 업데이트
        binding.textViewResult.text = "이미지 분석 중..."

        val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val result = visionText.text

                if (result.isEmpty()) {
                    binding.textViewResult.text = "📋 텍스트를 찾을 수 없습니다.\n다른 이미지를 선택해보세요."
                    return@addOnSuccessListener
                }

                binding.textViewResult.text = "📝 텍스트 추출 완료!\n분석 중..."
                analyzeWithGroq(result)
            }
            .addOnFailureListener { e ->
                binding.textViewResult.text = "❌ OCR 실패: ${e.message}\n다시 시도해보세요."
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
                    binding.textViewResult.text = "🌐 네트워크 오류: ${e.message}\n인터넷 연결을 확인해주세요."
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (!response.isSuccessful || body == null) {
                    runOnUiThread {
                        binding.textViewResult.text = "⚠️ 서버 응답 오류\n잠시 후 다시 시도해주세요."
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
                        // 🎨 결과에 따라 아이콘 추가
                        val resultWithIcon = when {
                            message.contains("사기") || message.contains("위험") -> "⚠️ $message"
                            message.contains("정상") || message.contains("안전") -> "✅ $message"
                            else -> "🔍 $message"
                        }
                        binding.textViewResult.text = resultWithIcon
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        binding.textViewResult.text = "📊 분석 완료!\n결과 처리 중 오류가 발생했습니다."
                    }
                }
            }
        })
    }
}