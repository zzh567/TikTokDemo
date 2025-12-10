package com.zzh.tiktokdemo.vedioclass

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoItem(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("video_url") val videoUrl: String,
    @SerializedName("cover_url") val coverUrl: String,
    @SerializedName("img_width") val width: Int,
    @SerializedName("img_height") val height: Int,
    @SerializedName("like_count") val likeCount: Int,
    @SerializedName("author") val author: String
) : Parcelable