package com.example.smartfinanceassistance

import android.os.Bundle
import android.util.Log
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
    }

}
