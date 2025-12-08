package com.zzh.tiktokdemo.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// 1. 定义接口
interface ApiService {
    @GET("feed.json")
    suspend fun getFeed(): List<com.zzh.tiktokdemo.vedioclass.VideoItem>
}

// 2. 单例客户端
object RetrofitClient {
    // 模拟器访问电脑本地 localhost 必须用 10.0.2.2
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val service: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}