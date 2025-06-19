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
import androidx.lifecycle.lifecycleScope
import com.example.smartfinanceassistance.MainActivity
import com.example.smartfinanceassistance.databinding.ActivityOcrProcessingBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.TextRecognition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class OCRProcessingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOcrProcessingBinding
    private var currentImageUri: String? = null
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOcrProcessingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = intent.getStringExtra("image_uri") ?: return
        currentImageUri = imageUri

        val bitmap = getBitmapFromUri(Uri.parse(imageUri))
        binding.imageView.setImageBitmap(bitmap)

        // 버튼 클릭 리스너 추가
        setupButtonListeners()

        // Coroutine으로 텍스트 인식 시작
        lifecycleScope.launch {
            runTextRecognition(bitmap)
        }
    }

    // 버튼 이벤트 설정
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

    private suspend fun runTextRecognition(bitmap: Bitmap) {
        try {
            // 분석 시작 시 UI 업데이트
            binding.textViewResult.text = "이미지 분석 중..."

            val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            val image = InputImage.fromBitmap(bitmap, 0)

            // ML Kit의 비동기 작업을 Coroutine으로 변환
            val visionText = withContext(Dispatchers.Default) {
                recognizer.process(image).await()
            }

            val result = visionText.text

            if (result.isEmpty()) {
                binding.textViewResult.text = "📋 텍스트를 찾을 수 없습니다.\n다른 이미지를 선택해보세요."
                return
            }

            binding.textViewResult.text = "📝 텍스트 추출 완료!\n분석 중..."

            // Groq API 분석 실행
            analyzeWithGroq(result)

        } catch (e: Exception) {
            binding.textViewResult.text = "❌ OCR 실패: ${e.message}\n다시 시도해보세요."
        }
    }

    private fun analyzeWithGroq(resultText: String) {
        lifecycleScope.launch {
            try {
                val analysisResult = withContext(Dispatchers.IO) {
                    performGroqAnalysis(resultText)
                }

                // UI 업데이트는 메인 스레드에서
                val resultWithIcon = when {
                    analysisResult.contains("사기") || analysisResult.contains("위험") -> "⚠️ $analysisResult"
                    analysisResult.contains("정상") || analysisResult.contains("안전") -> "✅ $analysisResult"
                    else -> "🔍 $analysisResult"
                }
                binding.textViewResult.text = resultWithIcon

            } catch (e: IOException) {
                binding.textViewResult.text = "🌐 네트워크 오류: ${e.message}\n인터넷 연결을 확인해주세요."
            } catch (e: Exception) {
                binding.textViewResult.text = "📊 분석 완료!\n결과 처리 중 오류가 발생했습니다."
            }
        }
    }

    private suspend fun performGroqAnalysis(resultText: String): String {
        val mediaType = "application/json".toMediaType()

        // 네트워크 연결 확인을 위한 로그 추가
        android.util.Log.d("OCR", "Groq API 호출 시작")

        val messagesArray = org.json.JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", """
                    당신은 금융 사기 여부를 판단하는 전문 분석가입니다.
                    사용자의 메시지를 보고 오직 아래 두 가지 중 하나로 판단해주세요:
                    
                    금융 사기는 다음 특징을 가짐:
                    1. 비현실적 수익 약속 (예: "하루 100% 수익", 키워드: "고수익", "보장", "배당").
                    2. 긴급 행동 유도 (예: "지금 송금", 키워드: "즉시", "급히", "마감").
                    3. 개인정보 요구 (예: "계좌번호 입력", 키워드: "계좌", "비밀번호", "신분증").
                    4. 의심스러운 링크/연락처 (예: 단축 URL처럼 "bit.ly/abc123", 비표준 도메인 ".xyz", 전화번호 "010-XXXX-XXXX", 카카오톡 ID 요청, 문맥상 "여기 클릭" 같은 유도 문구 포함).
                    5. 모호한 출처 (예: "공식 기관", 출처 불명확).

                    사기 아님은 다음 경우:
                    1. 일상적 대화 (금융 언급 없음, 예: 친구 간 대화).
                    2. 공식 출처의 광고/공지 (예: "국민은행 공식 사이트 https://www.kbstar.com", 기관명 명시, 비현실적 약속 없음, HTTPS 링크, 표준 문구 사용).
                    3. 금융 관련이지만 안전 (예: 카드 명세서, 결제 알림, 개인정보 요구 없음).
                    4. 링크 포함 시, 공식 기관명(예: "국민은행", "삼성카드" 등)과 HTTPS 링크가 문맥상 일치함.

                    출력 형식:
                    ```
                    결과: [사기 아님 / 사기 의심 됨]
                    이유: [텍스트에서 발견된 증거 또는 사기 특징 없는 이유]
                    ```
                    한글로 대답해.
                   
                    메시지 형식이 문자/카톡/이메일/알림/웹 등 무엇이든 위 기준을 동일하게 적용하세요.
                    판단이 모호할 경우, 사기로 간주하여 보수적으로 판단하십시오.
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

        android.util.Log.d("OCR", "요청 URL: ${request.url}")

        val response = client.newCall(request).execute()
        android.util.Log.d("OCR", "응답 코드: ${response.code}")

        val body = response.body?.string()

        if (!response.isSuccessful || body == null) {
            android.util.Log.e("OCR", "API 응답 실패: ${response.code}, ${response.message}")
            throw IOException("서버 응답 오류: ${response.code}")
        }

        return try {
            JSONObject(body)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        } catch (e: Exception) {
            throw Exception("응답 파싱 오류: ${e.message}")
        }
    }
}