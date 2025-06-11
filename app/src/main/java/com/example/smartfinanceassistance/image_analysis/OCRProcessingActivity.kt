package com.example.smartfinanceassistance.image_analysis

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.smartfinanceassistance.R
import com.example.smartfinanceassistance.databinding.ActivityOcrProcessingBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.TextRecognition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

        // OCR 처리
        recognizeTextWithMLKit(bitmap)
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val original = if (Build.VERSION.SDK_INT >= 29) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }

        return original.copy(Bitmap.Config.ARGB_8888, true)
    }

    private fun recognizeTextWithMLKit(bitmap: Bitmap) {
        val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text.trim()
                binding.textViewResult.text = if (extractedText.isEmpty()) {
                    "❗ 텍스트를 인식할 수 없습니다."
                } else {
                    extractedText
                }
                if (extractedText.isNotEmpty()) {
                    analyzeWithGroq(extractedText)
                }
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
                    """.trimIndent())
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
                    binding.aiResultText.text = "API 호출 실패: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (!response.isSuccessful || body == null) {
                    runOnUiThread {
                        binding.aiResultText.text = "응답 오류: $body"
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
                        binding.aiResultText.text = message
                        updateResultIcon(message)
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        binding.aiResultText.text = "응답 파싱 오류: ${e.message}"
                    }
                }
            }
        })
    }

    private fun updateResultIcon(message: String) {
        val iconView = binding.resultIcon
        val aiResultText = binding.aiResultText

        val lines = message.lines()
        val firstLine = lines.firstOrNull()?.lowercase()?.trim() ?: ""

        aiResultText.text = message  // 전체 결과는 항상 표시

        when {
            firstLine.contains("사기 아님") -> {
                iconView.setImageResource(R.drawable.ic_check_green)
                iconView.visibility = ImageView.VISIBLE
            }
            firstLine.contains("사기 의심") -> {
                iconView.setImageResource(R.drawable.ic_warning_red)
                iconView.visibility = ImageView.VISIBLE
            }
            else -> {
                iconView.visibility = ImageView.GONE
            }
        }
    }
}