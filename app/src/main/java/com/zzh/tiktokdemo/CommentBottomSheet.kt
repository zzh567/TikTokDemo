package com.zzh.tiktokdemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zzh.tiktokdemo.databinding.DialogCommentBinding
import com.zzh.tiktokdemo.vedioclass.Comment
import com.zzh.tiktokdemo.vedioclass.VideoItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentBottomSheet(
    private val videoItem: VideoItem,
    private val onCommentCountChanged: (Int) -> Unit // 回调：通知外部更新数字
) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogCommentBinding
    private val comments = ArrayList<Comment>()
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogCommentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 初始化列表
        commentAdapter = CommentAdapter(comments)
        binding.rvComments.layoutManager = LinearLayoutManager(context)
        binding.rvComments.adapter = commentAdapter

        binding.ivClose.setOnClickListener { dismiss() }

        // 2. 模拟从服务器拉取数据
        loadMockComments()

        // 3. 发送评论
        binding.ivSend.setOnClickListener {
            val content = binding.etComment.text.toString()
            if (content.isNotEmpty()) {
                sendComment(content)
            } else {
                Toast.makeText(context, "写点什么吧", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMockComments() {
        // 模拟：根据 videoItem.collectCount 随机生成一点评论，让不同视频看起来不一样
        comments.clear()
        val randomCount = videoItem.commentCount
        for (i in 1..randomCount) {
            comments.add(
                Comment(
                    id = "$i",
                    avatarUrl = "",
                    username = "用户${(1000..9999).random()}",
                    content = listOf("太棒了！", "学到了", "这个视频很有趣", "哈哈哈哈", "666").random(),
                    date = "${(1..24).random()}小时前"
                )
            )
        }
        updateTitle(comments.size)
        commentAdapter.notifyDataSetChanged()
    }

    private fun sendComment(content: String) {
        // 1. 构造新评论
        val newComment = Comment(
            id = System.currentTimeMillis().toString(),
            avatarUrl = "",
            username = "我", // 当前用户
            content = content,
            date = "刚刚"
        )

        // 2. 加到列表顶部
        commentAdapter.addComment(newComment)
        binding.rvComments.scrollToPosition(0) // 滚到顶部
        binding.etComment.setText("") // 清空输入框

        // 3. 更新 UI 和外部数据
        updateTitle(comments.size)

        // 4. 🔥 核心：通知外部 (PlayerAdapter) 数字变了
        // 我们假设现在的 total = 初始 + 新增
        // 为了简单，直接让外部 +1 即可，或者回传总数
        videoItem.commentCount++
        onCommentCountChanged(videoItem.commentCount)

        Toast.makeText(context, "评论成功", Toast.LENGTH_SHORT).show()
    }

    private fun updateTitle(count: Int) {
        binding.tvTitle.text = "评论 ($count)"
    }
}