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
import com.example.smartfinanceassistance.util.CaseSeeder
import com.example.smartfinanceassistance.util.FirestoreHelper
import com.example.smartfinanceassistance.util.QuizSeeder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GlobalScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)

            val quizDao = db.quizDao()
            if (quizDao.getAll().isEmpty()) {
                quizDao.insertAll(QuizSeeder.getSample())
                Log.d("MainActivity", "퀴즈 삽입 완료")
            }

            val caseDao = db.caseDao()
            val sampleCases = CaseSeeder.getSample()

            val distinctTypes = sampleCases.map { it.type }.distinct()

            for (type in distinctTypes) {
                if (caseDao.getCasesByType(type).isEmpty()) {
                    val casesToInsert = sampleCases.filter { it.type == type }
                    caseDao.insertAll(casesToInsert)
                    Log.d("MainActivity", "[$type] 사례 삽입 완료")
                }
            }
        }

    }
}