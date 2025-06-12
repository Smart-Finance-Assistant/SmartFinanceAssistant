package com.example.smartfinanceassistance.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartfinanceassistance.MainActivity
import com.example.smartfinanceassistance.R
import com.google.firebase.firestore.FirebaseFirestore

class NicknameActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var editText: EditText
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val savedNickname = prefs.getString("nickname", null)
        if (savedNickname != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        setContentView(R.layout.activity_nickname)

        editText = findViewById(R.id.editTextNickname)
        button = findViewById(R.id.buttonStart)

        button.setOnClickListener {
            val nickname = editText.text.toString().trim()
            if (nickname.isEmpty()) {
                Toast.makeText(this, "닉네임을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkNicknameDuplicate(nickname) { isDuplicate ->
                if (isDuplicate) {
                    Toast.makeText(this, "이미 사용 중인 닉네임입니다", Toast.LENGTH_SHORT).show()
                } else {
                    // 저장 후 MainActivity 이동
                    prefs.edit().putString("nickname", nickname).apply()
                    db.collection("quiz_scores")
                        .document(nickname)
                        .set(mapOf("createdAt" to System.currentTimeMillis()))

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun checkNicknameDuplicate(nickname: String, callback: (Boolean) -> Unit) {
        db.collection("quiz_scores")
            .document(nickname)
            .get()
            .addOnSuccessListener { document ->
                callback(document.exists())
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "닉네임 중복 확인 실패", e)
                callback(false) // 실패 시 일단 허용
            }
    }
}