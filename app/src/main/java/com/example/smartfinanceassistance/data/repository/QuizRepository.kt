package com.example.smartfinanceassistance.data.repository

import com.example.smartfinanceassistance.data.db.QuizDao
import com.example.smartfinanceassistance.data.db.QuizEntity

class QuizRepository(private val dao: QuizDao) {
    suspend fun getAllQuizzes() = dao.getAll()
    suspend fun insertAll(quizzes: List<QuizEntity>) = dao.insertAll(quizzes)
}
