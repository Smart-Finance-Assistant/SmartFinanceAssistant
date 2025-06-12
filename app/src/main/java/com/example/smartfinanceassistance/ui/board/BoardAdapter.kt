package com.example.smartfinanceassistance.ui.board

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfinanceassistance.R
import com.example.smartfinanceassistance.data.model.BoardPost

class BoardAdapter(
    private val posts: List<BoardPost>,
    private val onItemClick: (BoardPost) -> Unit
) : RecyclerView.Adapter<BoardAdapter.BoardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_board_post, parent, false)
        return BoardViewHolder(view)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int = posts.size

    inner class BoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.text_title)
        private val contentPreview: TextView = itemView.findViewById(R.id.text_content_preview)
        private val authorText: TextView = itemView.findViewById(R.id.text_author)
        private val dateText: TextView = itemView.findViewById(R.id.text_date)
        private val viewCountText: TextView = itemView.findViewById(R.id.text_view_count)
        private val likeCountText: TextView = itemView.findViewById(R.id.text_like_count)

        fun bind(post: BoardPost) {
            titleText.text = post.title

            // 내용 미리보기 (50자 제한)
            contentPreview.text = if (post.content.length > 50) {
                post.content.substring(0, 50) + "..."
            } else {
                post.content
            }

            authorText.text = post.author
            dateText.text = post.getFormattedDate()
            viewCountText.text = "조회 ${post.viewCount}"
            likeCountText.text = "추천 ${post.likeCount}"

            itemView.setOnClickListener {
                onItemClick(post)
            }
        }
    }
}