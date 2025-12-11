package main

import (
	"encoding/json"
	"log"
	"math/rand"
	"net/http"
	"os"
	"time"
)

type VideoData map[string]interface{}

func main() {
	// 1. 设置随机数种子 (保证每次启动随机性不同)
	rand.Seed(time.Now().UnixNano())

	// 2. 创建文件服务器 handler (用于处理视频、图片等静态资源)
	// http.Dir(".") 表示当前目录
	fileServer := http.FileServer(http.Dir("."))

	// 3. 自定义路由处理函数
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {

		// 核心拦截逻辑：如果是请求 feed 数据
		if r.URL.Path == "/feed.json" {
			handleFeedRequest(w, r)
			return
		}

		// 其他请求 (如 .mp4, .jpg)，直接交给静态文件服务器处理
		fileServer.ServeHTTP(w, r)
	})

	log.Println("Go 动态随机服务器已启动 :8080 ...")
	log.Println("每次请求 /feed.json 都会返回随机顺序的数据")

	// 启动监听
	if err := http.ListenAndServe(":8080", nil); err != nil {
		log.Fatal(err)
	}
}

// 处理 Feed 请求的具体逻辑
func handleFeedRequest(w http.ResponseWriter, r *http.Request) {
	// A. 读取本地 JSON 文件
	bytes, err := os.ReadFile("feed.json")
	if err != nil {
		http.Error(w, "无法读取数据文件", http.StatusInternalServerError)
		log.Println("❌ 读取 feed.json 失败:", err)
		return
	}

	// B. 解析 JSON 到切片中
	var videos []VideoData
	if err := json.Unmarshal(bytes, &videos); err != nil {
		http.Error(w, "数据格式错误", http.StatusInternalServerError)
		log.Println("❌ 解析 JSON 失败:", err)
		return
	}

	// C. 核心算法：洗牌 (Shuffle)
	// 遍历数组，随机交换位置
	rand.Shuffle(len(videos), func(i, j int) {
		videos[i], videos[j] = videos[j], videos[i]
	})

	// D. 设置响应头 (告诉客户端这是 JSON)
	w.Header().Set("Content-Type", "application/json")
	w.Header().Set("Cache-Control", "no-cache, no-store, must-revalidate")
	w.Header().Set("Pragma", "no-cache")
	w.Header().Set("Expires", "0")
	w.WriteHeader(http.StatusOK)

	// E. 将打乱后的数据编码回 JSON 并发送
	if err := json.NewEncoder(w).Encode(videos); err != nil {
		log.Println("发送响应失败:", err)
	}

	log.Printf("已响应随机列表请求 (共 %d 条视频)", len(videos))
}
