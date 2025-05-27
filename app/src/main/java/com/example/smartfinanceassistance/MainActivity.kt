package com.example.smartfinanceassistance

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.smartfinanceassistance.data.db.AppDatabase
import com.example.smartfinanceassistance.ui.theme.SmartFinanceAssistanceTheme
import com.example.smartfinanceassistance.util.FirestoreHelper
import com.example.smartfinanceassistance.util.QuizSeeder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GlobalScope.launch {
            val dao = AppDatabase.getDatabase(applicationContext).quizDao()
            val size = dao.getAll().size
            Log.d("MainActivity", "현재 퀴즈 수: $size")
            if (size == 0) {
                dao.insertAll(QuizSeeder.getSample())
                Log.d("MainActivity", "퀴즈 삽입 완료")
            }
        }

        val button = findViewById<Button>(R.id.buttonSaveTestScore)
        button.setOnClickListener {
            Log.d("FirestoreTest", "버튼 클릭됨")

            val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val nickname = prefs.getString("nickname", null)
            Log.d("FirestoreTest", "nickname: $nickname")

            if (nickname == null) {
                Log.e("FirestoreTest", "nickname 없음. 저장 중단됨")
            } else {
                FirestoreHelper.saveScore(this, "voicephishing", 4)
            }
        }
    }

}
