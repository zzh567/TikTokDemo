package com.zzh.tiktokdemo.vedioclass

data class ChatMessage(
    val content: String,
    val isFromUser: Boolean // true=用户发的, false=AI回的
)