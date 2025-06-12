package com.example.smartfinanceassistance.ui.quiz

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartfinanceassistance.data.db.AppDatabase
import com.example.smartfinanceassistance.data.db.QuizEntity
import com.example.smartfinanceassistance.data.repository.QuizRepository
import kotlinx.coroutines.launch

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val quizDao = AppDatabase.getDatabase(application).quizDao()
    private val repository = QuizRepository(quizDao)

    private val _quizzes = MutableLiveData<List<QuizEntity>>()
    val quizzes: LiveData<List<QuizEntity>> get() = _quizzes

    val userAnswers = mutableListOf<Pair<QuizEntity, Boolean>>() // 유저 선택 저장

    fun loadQuizzes() {
        viewModelScope.launch {
            val loaded = repository.getAllQuizzes()
            Log.d("QuizViewModel", "불러온 퀴즈 수: ${loaded.size}")
            _quizzes.postValue(loaded)
        }
    }

    fun recordAnswer(quiz: QuizEntity, choice: Boolean) {
        userAnswers.add(Pair(quiz, choice))
    }

    fun getWeakTypes(): List<String> {
        // 유형별로 정답 수/전체 수 카운트용 맵
        val stats = mutableMapOf<String, Pair<Int, Int>>()  // (정답 수, 전체 수)

        for ((quiz, userChoice) in userAnswers) {
            val type = quiz.type
            val isCorrect = quiz.answer == userChoice

            val (correct, total) = stats[type] ?: Pair(0, 0)
            val newCorrect = if (isCorrect) correct + 1 else correct
            stats[type] = Pair(newCorrect, total + 1)
        }

        // 정답률 40% 이하인 유형만 필터링
        return stats.filter { (_, result) ->
            val (correct, total) = result
            total > 0 && (correct.toDouble() / total.toDouble() <= 0.4)
        }.keys.toList()
    }

    // 🆕 모든 유형별 점수 반환
    fun getAllTypeScores(): List<Map<String, Any>> {
        val stats = mutableMapOf<String, Pair<Int, Int>>()  // (정답 수, 전체 수)

        for ((quiz, userChoice) in userAnswers) {
            val type = quiz.type
            val isCorrect = quiz.answer == userChoice

            val (correct, total) = stats[type] ?: Pair(0, 0)
            val newCorrect = if (isCorrect) correct + 1 else correct
            stats[type] = Pair(newCorrect, total + 1)
        }

        return stats.map { (type, result) ->
            val (correct, total) = result
            val percentage = if (total > 0) (correct * 100 / total) else 0
            mapOf(
                "type" to type,
                "correctCount" to correct,
                "totalCount" to total,
                "percentage" to percentage,
                "isWeak" to (percentage <= 40)
            )
        }
    }

    // 🆕 가장 취약한 유형 하나만 반환 (정답률이 가장 낮은 것)
    fun getMostWeakType(): Map<String, Any>? {
        val allScores = getAllTypeScores()
        return allScores.filter { it["isWeak"] as Boolean }
            .minByOrNull { it["percentage"] as Int }
    }
    // 🆕 이 메서드를 추가하세요!
    fun resetQuiz() {
        userAnswers.clear()
        Log.d("QuizViewModel", "퀴즈 데이터 초기화됨 - 이전 답변 삭제")
    }
    // 🆕 전체 정답률 계산
    fun getOverallScore(): Pair<Int, Int> {
        val correct = userAnswers.count { (quiz, userChoice) -> quiz.answer == userChoice }
        val total = userAnswers.size
        return Pair(correct, total)
    }
}