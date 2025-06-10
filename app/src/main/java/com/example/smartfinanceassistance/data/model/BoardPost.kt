// BoardPost.kt - 게시글 데이터 클래스
package com.example.smartfinanceassistance.data.model

data class BoardPost(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val author: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val viewCount: Int = 0,
    val likeCount: Int = 0,
    val likedUsers: List<String> = emptyList() // 추천한 사용자 목록
) {
    // Firebase용 기본 생성자
    constructor() : this("", "", "", "", 0L, 0, 0, emptyList())

    // 작성일 포맷팅
    fun getFormattedDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy.MM.dd HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    // 사용자가 추천했는지 확인
    fun isLikedBy(username: String): Boolean {
        return likedUsers.contains(username)
    }
}