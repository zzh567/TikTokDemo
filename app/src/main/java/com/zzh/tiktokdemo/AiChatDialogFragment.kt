package com.zzh.tiktokdemo

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.zzh.tiktokdemo.databinding.DialogAiChatBinding
import com.zzh.tiktokdemo.vedioclass.ChatMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AiChatDialogFragment : DialogFragment() {

    private lateinit var binding: DialogAiChatBinding
    private val messageList = ArrayList<ChatMessage>()
    private lateinit var chatAdapter: AiChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置全屏样式
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogAiChatBinding.inflate(inflater, container, false)
        // 设置 Dialog 全屏宽高
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 初始化列表
        chatAdapter = AiChatAdapter(messageList)
        binding.rvChatList.layoutManager = LinearLayoutManager(context)
        binding.rvChatList.adapter = chatAdapter

        // 添加一条欢迎语
        addAiMessage("你好！我是你的 AI 视频助手，有什么可以帮你的吗？")

        // 2. 事件监听
        binding.ivCloseChat.setOnClickListener { dismiss() }

        binding.btnSendChat.setOnClickListener {
            val input = binding.etChatInput.text.toString().trim()
            if (input.isNotEmpty()) {
                sendMessage(input)
            }
        }
    }

    private fun sendMessage(content: String) {
        // 1. 发送用户消息
        val userMsg = ChatMessage(content, isFromUser = true)
        chatAdapter.addMessage(userMsg, binding.rvChatList)
        binding.etChatInput.setText("")

        // 2. 🔥 模拟 AI 回复 (这里替换成真实的 API 调用)
        // 使用协程在后台模拟耗时
        lifecycleScope.launch {
            // 模拟网络延迟 1-3秒
            delay((1000..3000).random().toLong())

            // 生成模拟回复
            val mockReply = mockAiResponse(content)
            addAiMessage(mockReply)
        }
    }

    private fun addAiMessage(content: String) {
        val aiMsg = ChatMessage(content, isFromUser = false)
        chatAdapter.addMessage(aiMsg, binding.rvChatList)
    }

    // 🔥🔥🔥 Mock AI 回复逻辑 (未来在这里接入 ChatGPT/文心一言等 API)
    private fun mockAiResponse(userQuery: String): String {
        return when {
            userQuery.contains("你好") -> "你好呀！很高兴见到你。"
            userQuery.contains("视频") -> "这都是为你精选的有趣视频哦！"
            userQuery.contains("点赞") -> "双击屏幕就可以快速点赞啦！"
            userQuery.contains("作者") -> "点击右侧的头像可以查看作者详情，还可以换头像哦。"
            userQuery.length < 5 -> "嗯嗯，我在听。"
            else -> "这个问题很有深度，让我想想... (假装在思考) 我觉得你说得对！"
        }
    }
}