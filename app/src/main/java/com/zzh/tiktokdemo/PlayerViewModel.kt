package com.zzh.tiktokdemo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zzh.tiktokdemo.network.RetrofitClient
import com.zzh.tiktokdemo.vedioclass.VideoItem
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {

    // 只负责通知有新数据来了
    val newVideoList = MutableLiveData<List<VideoItem>>()

    // 失败/结束回调
    val loadState = MutableLiveData<Boolean>() // true=成功, false=失败

    fun loadMore() {
        viewModelScope.launch {
            try {
                // 模拟网络请求
                val videos = RetrofitClient.service.getFeed()
                // 模拟随机排序，产生“新数据”的感觉
                newVideoList.value = videos.shuffled()
                loadState.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                loadState.value = false
            }
        }
    }

    // 播放页的“下拉刷新”通常意味着重置整个列表（慎用，会打断当前播放）
    // 或者你可以做成“获取往期推荐”
    fun refresh() {
        // 逻辑同 loadMore，只是 UI 处理不同
        loadMore()
    }
}