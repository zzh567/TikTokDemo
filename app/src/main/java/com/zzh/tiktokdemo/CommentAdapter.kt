package com.zzh.tiktokdemo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zzh.tiktokdemo.databinding.ItemCommentBinding
import com.zzh.tiktokdemo.vedioclass.Comment

class CommentAdapter(private val comments: ArrayList<Comment>) :
    RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = comments[position]
        holder.binding.tvUsername.text = item.username
        holder.binding.tvContent.text = item.content
        holder.binding.tvDate.text = item.date
        // 这里可以用 Glide 加载头像 holder.binding.ivAvatar...
    }

    override fun getItemCount() = comments.size

    // 添加单条评论到头部
    fun addComment(comment: Comment) {
        comments.add(0, comment)
        notifyItemInserted(0)
    }
}