package com.example.smartfinanceassistance.ui.board

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfinanceassistance.R
import com.example.smartfinanceassistance.data.model.BoardPost
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class BoardListFragment : Fragment(R.layout.fragment_board_list) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BoardAdapter
    private val posts = mutableListOf<BoardPost>()
    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupRecyclerView()
        loadPosts()
    }

    private fun setupViews(view: View) {
        // 뒤로가기 버튼
        view.findViewById<Button>(R.id.btn_back).setOnClickListener {
            findNavController().navigateUp()
        }

        // 글쓰기 버튼
        view.findViewById<Button>(R.id.btn_write).setOnClickListener {
            val action = BoardListFragmentDirections.actionBoardListFragmentToBoardWriteFragment(null)
            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerView() {
        recyclerView = view?.findViewById(R.id.recycler_board) ?: return

        adapter = BoardAdapter(posts) { post ->
            // 게시글 클릭 시 상세보기로 이동
            val action = BoardListFragmentDirections.actionBoardListFragmentToBoardDetailFragment(post.id)
            findNavController().navigate(action)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun loadPosts() {
        db.collection("board_posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("BoardList", "게시글 로드 실패", error)
                    return@addSnapshotListener
                }

                posts.clear()
                snapshot?.documents?.forEach { document ->
                    try {
                        val post = document.toObject(BoardPost::class.java)?.copy(id = document.id)
                        post?.let { posts.add(it) }
                    } catch (e: Exception) {
                        Log.e("BoardList", "게시글 파싱 실패: ${document.id}", e)
                    }
                }

                adapter.notifyDataSetChanged()
                updateEmptyState()

                Log.d("BoardList", "게시글 ${posts.size}개 로드됨")
            }
    }

    private fun updateEmptyState() {
        val emptyView = view?.findViewById<TextView>(R.id.empty_view)
        emptyView?.visibility = if (posts.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (posts.isEmpty()) View.GONE else View.VISIBLE
    }
}