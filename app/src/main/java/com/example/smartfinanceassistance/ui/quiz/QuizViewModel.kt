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

}
