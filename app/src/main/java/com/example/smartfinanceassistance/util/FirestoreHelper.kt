package com.example.smartfinanceassistance.util

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreHelper {
    fun saveScore(context: Context, category: String, score: Int) {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val nickname = prefs.getString("nickname", null) ?: return

        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("quiz_scores").document(nickname)

        docRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                docRef.update(category, score)
                    .addOnSuccessListener { Log.d("Firestore", "점수 업데이트 완료") }
            } else {
                val newData = mutableMapOf(
                    category to score,
                    "createdAt" to System.currentTimeMillis()
                )
                docRef.set(newData)
                    .addOnSuccessListener { Log.d("Firestore", "점수 저장 완료") }
            }
        }
    }
}
