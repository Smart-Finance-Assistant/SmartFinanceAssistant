package com.example.smartfinanceassistance.externalApp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.smartfinanceassistance.R

class CallActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        // 버튼 클릭 리스너 설정
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // 금융감독원 카드 클릭
        findViewById<View>(R.id.btn_call_fss).setOnClickListener {
            dialPhoneNumber(this, "1332")
        }

        // 경찰서 카드 클릭
        findViewById<View>(R.id.btn_call_police).setOnClickListener {
            dialPhoneNumber(this, "112")
        }
    }

    private fun dialPhoneNumber(context: Context, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("PhoneIntent", "전화 앱 실행 오류: ${e.localizedMessage}")
        }
        // finish()를 제거하여 화면이 유지되도록 함
    }
}