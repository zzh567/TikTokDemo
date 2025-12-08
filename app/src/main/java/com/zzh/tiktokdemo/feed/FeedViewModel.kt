package com.zzh.tiktokdemo.feed

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zzh.tiktokdemo.vedioclass.VideoItem
import com.zzh.tiktokdemo.network.RetrofitClient
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {

    // 1. 私有的 MutableLiveData，只有 ViewModel 自己能修改数据 (封装性)
    private val _videoList = MutableLiveData<List<VideoItem>>()

    // 2. 公开的 LiveData，UI 只能观察 (Observer)，不能修改 (只读)
    val videoList: LiveData<List<VideoItem>> = _videoList

    // 增加一个 Loading 状态，方便 UI 显示加载圈
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // 初始化时自动拉取一次数据
    init {
        fetchVideoList()
    }

    /**
     * 核心业务逻辑：拉取视频列表
     */
    fun fetchVideoList() {
        // 开启协程 (相当于 Go 的 go func()，但绑定了 ViewModel 的生命周期)
        viewModelScope.launch {
            try {
                _isLoading.value = true // 开始加载

                // 3. 这里的代码看似同步，其实是异步挂起的 (Retrofit suspend)
                val videos = RetrofitClient.service.getFeed()

                // 4. 拿到数据，通知 UI 更新
                Log.d("FeedViewModel", "成功获取 ${videos.size} 条数据")
                _videoList.value = videos

            } catch (e: Exception) {
                Log.e("FeedViewModel", "网络请求失败", e)
                // 实际项目中这里应该通过 LiveData 通知 UI 弹出 Toast
            } finally {
                _isLoading.value = false // 结束加载
            }
        }
    }
}