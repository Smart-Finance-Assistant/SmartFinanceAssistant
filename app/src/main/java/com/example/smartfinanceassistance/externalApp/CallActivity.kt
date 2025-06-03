package com.example.smartfinanceassistance.externalApp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class CallActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // layout 없이 다이얼로그만 띄우고 전화 실행
        showCallChoiceDialog()
    }

    private fun showCallChoiceDialog() {
        val items = arrayOf("112 (경찰)", "1332 (금융감독원)")

        AlertDialog.Builder(this)
            .setTitle("전화 연결 대상 선택")
            .setItems(items) { _, which ->
                val number = if (which == 0) "112" else "1332"
                dialPhoneNumber(this, number)
            }
            .setNegativeButton("취소") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun dialPhoneNumber(context: Context, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("PhoneIntent", "전화 앱 실행 오류: ${e.localizedMessage}")
        } finally {
            finish() // 전화 앱 넘어가도 이 액티비티는 종료
        }
    }
}