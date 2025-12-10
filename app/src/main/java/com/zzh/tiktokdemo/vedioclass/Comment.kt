package com.zzh.tiktokdemo.vedioclass

data class Comment(
    val id: String,
    val avatarUrl: String, // 头像
    val username: String,  // 用户名
    val content: String,   // 内容
    val date: String,      // 时间
    var likeCount: Int = 0,
    var isLiked: Boolean = false
)