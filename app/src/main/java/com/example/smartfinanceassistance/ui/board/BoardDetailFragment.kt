package com.example.smartfinanceassistance.ui.board

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.smartfinanceassistance.R
import com.example.smartfinanceassistance.data.model.BoardPost
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class BoardDetailFragment : Fragment(R.layout.fragment_board_detail) {

    private val args: BoardDetailFragmentArgs by navArgs()
    private val db = FirebaseFirestore.getInstance()
    private var currentPost: BoardPost? = null
    private var currentUser: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 현재 사용자 정보 가져오기
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        currentUser = prefs.getString("nickname", "") ?: ""

        setupViews(view)
        loadPost()
    }

    private fun setupViews(view: View) {
        // 뒤로가기 버튼
        view.findViewById<Button>(R.id.btn_back).setOnClickListener {
            findNavController().navigateUp()
        }

        // 추천 버튼
        view.findViewById<Button>(R.id.btn_like).setOnClickListener {
            toggleLike()
        }
    }

    private fun loadPost() {
        val postId = args.postId

        // 조회수 증가
        increaseViewCount(postId)

        // 게시글 데이터 로드
        db.collection("board_posts").document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    currentPost = document.toObject(BoardPost::class.java)?.copy(id = document.id)
                    currentPost?.let { post ->
                        displayPost(post)
                        setupActionButtons(post)
                    }
                } else {
                    Toast.makeText(requireContext(), "게시글을 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
            .addOnFailureListener { e ->
                Log.e("BoardDetail", "게시글 로드 실패", e)
                Toast.makeText(requireContext(), "게시글을 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
    }

    private fun increaseViewCount(postId: String) {
        db.collection("board_posts").document(postId)
            .update("viewCount", FieldValue.increment(1))
            .addOnSuccessListener {
                Log.d("BoardDetail", "조회수 증가 성공")
            }
            .addOnFailureListener { e ->
                Log.e("BoardDetail", "조회수 증가 실패", e)
            }
    }

    private fun displayPost(post: BoardPost) {
        view?.let { v ->
            v.findViewById<TextView>(R.id.text_title).text = post.title
            v.findViewById<TextView>(R.id.text_content).text = post.content
            v.findViewById<TextView>(R.id.text_author).text = post.author
            v.findViewById<TextView>(R.id.text_date).text = post.getFormattedDate()
            v.findViewById<TextView>(R.id.text_view_count).text = "조회 ${post.viewCount + 1}" // +1은 방금 증가한 조회수

            updateLikeButton(post)
        }
    }

    private fun updateLikeButton(post: BoardPost) {
        val likeButton = view?.findViewById<Button>(R.id.btn_like) ?: return
        val isLiked = post.isLikedBy(currentUser)

        likeButton.text = if (isLiked) {
            "❤️ 추천 ${post.likeCount}"
        } else {
            "🤍 추천 ${post.likeCount}"
        }

        likeButton.setBackgroundColor(
            if (isLiked) {
                resources.getColor(android.R.color.holo_red_light, null)
            } else {
                resources.getColor(android.R.color.darker_gray, null)
            }
        )
    }

    private fun setupActionButtons(post: BoardPost) {
        val actionButtonsLayout = view?.findViewById<LinearLayout>(R.id.action_buttons_layout) ?: return

        // 작성자가 현재 사용자와 같은 경우에만 수정/삭제 버튼 표시
        if (post.author == currentUser && currentUser.isNotEmpty()) {
            actionButtonsLayout.visibility = View.VISIBLE

            // 수정 버튼
            view?.findViewById<Button>(R.id.btn_edit)?.setOnClickListener {
                val action = BoardDetailFragmentDirections
                    .actionBoardDetailFragmentToBoardWriteFragment(post.id)
                findNavController().navigate(action)
            }

            // 삭제 버튼
            view?.findViewById<Button>(R.id.btn_delete)?.setOnClickListener {
                showDeleteConfirmDialog(post.id)
            }
        } else {
            actionButtonsLayout.visibility = View.GONE
        }
    }

    private fun toggleLike() {
        val post = currentPost ?: return
        val postId = args.postId

        if (currentUser.isEmpty()) {
            Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            return
        }

        val isCurrentlyLiked = post.isLikedBy(currentUser)
        val newLikedUsers = if (isCurrentlyLiked) {
            // 추천 취소
            post.likedUsers.filter { it != currentUser }
        } else {
            // 추천 추가
            post.likedUsers + currentUser
        }

        val updates = mapOf(
            "likedUsers" to newLikedUsers,
            "likeCount" to newLikedUsers.size
        )

        db.collection("board_posts").document(postId)
            .update(updates)
            .addOnSuccessListener {
                // UI 업데이트
                currentPost = post.copy(
                    likedUsers = newLikedUsers,
                    likeCount = newLikedUsers.size
                )
                currentPost?.let { updateLikeButton(it) }

                val message = if (isCurrentlyLiked) "추천을 취소했습니다" else "추천했습니다"
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                Log.d("BoardDetail", "추천 상태 변경 성공: ${!isCurrentlyLiked}")
            }
            .addOnFailureListener { e ->
                Log.e("BoardDetail", "추천 상태 변경 실패", e)
                Toast.makeText(requireContext(), "추천 처리에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteConfirmDialog(postId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("게시글 삭제")
            .setMessage("정말로 이 게시글을 삭제하시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                deletePost(postId)
            }
            .setNegativeButton("아니요", null)
            .show()
    }

    private fun deletePost(postId: String) {
        db.collection("board_posts").document(postId)
            .delete()
            .addOnSuccessListener {
                Log.d("BoardDetail", "게시글 삭제 성공: $postId")
                Toast.makeText(requireContext(), "게시글이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_boardDetailFragment_to_boardListFragment)
            }
            .addOnFailureListener { e ->
                Log.e("BoardDetail", "게시글 삭제 실패", e)
                Toast.makeText(requireContext(), "게시글 삭제에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
    }
}