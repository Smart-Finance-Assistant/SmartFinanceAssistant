package com.example.smartfinanceassistance.ui.board

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.smartfinanceassistance.R
import com.example.smartfinanceassistance.data.model.BoardPost
import com.google.firebase.firestore.FirebaseFirestore

class BoardWriteFragment : Fragment(R.layout.fragment_board_write) {

    private val args: BoardWriteFragmentArgs by navArgs()
    private val db = FirebaseFirestore.getInstance()
    private var isEditMode = false
    private var existingPost: BoardPost? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 수정 모드인지 확인
        isEditMode = args.postId != null

        setupViews(view)

        if (isEditMode) {
            loadPostForEdit()
        }
    }

    private fun setupViews(view: View) {
        val titleEdit = view.findViewById<EditText>(R.id.edit_title)
        val contentEdit = view.findViewById<EditText>(R.id.edit_content)
        val btnSubmit = view.findViewById<Button>(R.id.btn_submit)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)

        // 버튼 텍스트 설정
        btnSubmit.text = if (isEditMode) "수정완료" else "작성완료"

        // 완료 버튼
        btnSubmit.setOnClickListener {
            val title = titleEdit.text.toString().trim()
            val content = contentEdit.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "제목을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (content.isEmpty()) {
                Toast.makeText(requireContext(), "내용을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isEditMode) {
                updatePost(title, content)
            } else {
                createPost(title, content)
            }
        }

        // 취소 버튼
        btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadPostForEdit() {
        val postId = args.postId ?: return

        db.collection("board_posts").document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    existingPost = document.toObject(BoardPost::class.java)?.copy(id = document.id)
                    existingPost?.let { post ->
                        view?.findViewById<EditText>(R.id.edit_title)?.setText(post.title)
                        view?.findViewById<EditText>(R.id.edit_content)?.setText(post.content)
                    }
                } else {
                    Toast.makeText(requireContext(), "게시글을 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
            .addOnFailureListener { e ->
                Log.e("BoardWrite", "게시글 로드 실패", e)
                Toast.makeText(requireContext(), "게시글을 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
    }

    private fun createPost(title: String, content: String) {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val author = prefs.getString("nickname", "익명") ?: "익명"

        val post = BoardPost(
            title = title,
            content = content,
            author = author,
            timestamp = System.currentTimeMillis(),
            viewCount = 0,
            likeCount = 0,
            likedUsers = emptyList()
        )

        db.collection("board_posts")
            .add(post)
            .addOnSuccessListener { documentReference ->
                Log.d("BoardWrite", "게시글 작성 성공: ${documentReference.id}")
                Toast.makeText(requireContext(), "게시글이 작성되었습니다", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_boardWriteFragment_to_boardListFragment)
            }
            .addOnFailureListener { e ->
                Log.e("BoardWrite", "게시글 작성 실패", e)
                Toast.makeText(requireContext(), "게시글 작성에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updatePost(title: String, content: String) {
        val postId = args.postId ?: return
        val existing = existingPost ?: return

        val updatedPost = existing.copy(
            title = title,
            content = content
        )

        db.collection("board_posts").document(postId)
            .set(updatedPost)
            .addOnSuccessListener {
                Log.d("BoardWrite", "게시글 수정 성공: $postId")
                Toast.makeText(requireContext(), "게시글이 수정되었습니다", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_boardWriteFragment_to_boardListFragment)
            }
            .addOnFailureListener { e ->
                Log.e("BoardWrite", "게시글 수정 실패", e)
                Toast.makeText(requireContext(), "게시글 수정에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
    }
}