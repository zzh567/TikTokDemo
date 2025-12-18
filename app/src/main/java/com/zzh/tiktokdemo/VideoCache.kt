package com.zzh.tiktokdemo

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@UnstableApi
object VideoCache {
    private var simpleCache: SimpleCache? = null
    private var cacheDataSourceFactory: CacheDataSource.Factory? = null

    // 1. 初始化 Cache (务必在 Activity onCreate 或 Application 中调用)
    fun init(context: Context) {
        if (simpleCache != null) return

        // 缓存目录：/data/user/0/com.zzh.tiktokdemo/cache/tiktok_video_cache
        val cacheDir = File(context.cacheDir, "tiktok_video_cache")
        // 缓存策略：最多占用 200MB，超过后删除最近最少使用的
        val evictor = LeastRecentlyUsedCacheEvictor(200 * 1024 * 1024)
        val databaseProvider = StandaloneDatabaseProvider(context)

        simpleCache = SimpleCache(cacheDir, evictor, databaseProvider)

        val upstreamFactory = DefaultHttpDataSource.Factory()
        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache!!)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    // 2. 核心方法 A：构建一个优先读缓存的 MediaSource 给播放器用
    fun buildMediaSource(url: String): MediaSource {
        val mediaItem = MediaItem.fromUri(url)
        // 必须使用 ProgressiveMediaSource 配合 cacheDataSourceFactory 才能命中缓存
        return ProgressiveMediaSource.Factory(cacheDataSourceFactory!!)
            .createMediaSource(mediaItem)
    }

    // 3. 核心方法 B：静默下载下一个视频的开头部分 (预加载)
    fun preLoadNextVideo(url: String) {
        if (simpleCache == null || cacheDataSourceFactory == null) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 只下载前 500KB (或者 1MB)，足够起播即可，不要浪费流量
                val dataSpec = DataSpec.Builder()
                    .setUri(Uri.parse(url))
                    .setLength(500 * 1024)
                    .build()

                val dataSource = cacheDataSourceFactory!!.createDataSource()
                val cacheWriter = CacheWriter(dataSource, dataSpec, null, null)
                cacheWriter.cache() // 这是一个阻塞方法，所以要在协程里跑
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun release() {
        simpleCache?.release()
        simpleCache = null
    }
}