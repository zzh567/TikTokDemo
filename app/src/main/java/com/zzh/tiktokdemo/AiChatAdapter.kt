package com.zzh.tiktokdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zzh.tiktokdemo.databinding.ItemAiChatBinding
import com.zzh.tiktokdemo.vedioclass.ChatMessage

class AiChatAdapter(private val messages: ArrayList<ChatMessage>) :
    RecyclerView.Adapter<AiChatAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAiChatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAiChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        if (message.isFromUser) {
            // 显示用户的，隐藏 AI 的
            holder.binding.tvUserMessage.visibility = View.VISIBLE
            holder.binding.tvAiMessage.visibility = View.GONE
            holder.binding.tvUserMessage.text = message.content
        } else {
            // 显示 AI 的，隐藏用户的
            holder.binding.tvUserMessage.visibility = View.GONE
            holder.binding.tvAiMessage.visibility = View.VISIBLE
            holder.binding.tvAiMessage.text = message.content
        }
    }

    override fun getItemCount() = messages.size

    // 添加新消息并滚动到底部
    fun addMessage(message: ChatMessage, recyclerView: RecyclerView) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
    }
}