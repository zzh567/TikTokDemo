package com.zzh.tiktokdemo

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.RecyclerView
import com.zzh.tiktokdemo.databinding.ItemVideoPlayerBinding
import com.zzh.tiktokdemo.vedioclass.VideoItem

class PlayerAdapter(
    private val videoList: ArrayList<VideoItem>,
    private val startPosition: Int,
    private val onVideoReady: () -> Unit,
    private val onAvatarClick: (position : Int) -> Unit
) : RecyclerView.Adapter<PlayerAdapter.VideoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoPlayerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videoList[position], position == startPosition, onVideoReady, onAvatarClick, position)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty() && payloads[0] == "UPDATE_AVATAR") {
            // 如果收到了 "UPDATE_AVATAR" 的信号，只更新头像，不动播放器！
            holder.updateAvatar(videoList[position])
        } else {
            // 否则，走正常的完全绑定流程
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount(): Int = videoList.size

    // 追加数据
    fun addData(newVideos: List<VideoItem>) {
        val startPos = videoList.size
        videoList.addAll(newVideos)
        notifyItemRangeInserted(startPos, newVideos.size)
    }

    // 重置数据
    fun refreshData(newVideos: List<VideoItem>) {
        videoList.clear()
        videoList.addAll(newVideos)
        notifyDataSetChanged()
    }

    // 🔥 保留：这是 RecyclerView 内部的回收机制，滑出去必须释放
    override fun onViewDetachedFromWindow(holder: VideoViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.release()
    }

    class VideoViewHolder(
        private val binding: ItemVideoPlayerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var player: ExoPlayer? = null
        private var currentUrl: String? = null
        private var currentItem: VideoItem? = null

        private var rotateAnimator: android.animation.ObjectAnimator? = null

        fun updateAvatar(item: VideoItem) {
            val avatarToLoad = item.localAvatarUri ?: item.coverUrl

            // 更新侧边栏头像
            com.bumptech.glide.Glide.with(binding.root.context)
                .load(avatarToLoad)
                .circleCrop()
                .into(binding.ivAvatar)
        }

        private val gestureDetector = android.view.GestureDetector(binding.root.context,
            object : android.view.GestureDetector.SimpleOnGestureListener() {

                // 必须返回 true，否则接收不到后续事件
                override fun onDown(e: android.view.MotionEvent): Boolean = true

                // 🔥 单击确认：播放/暂停
                override fun onSingleTapConfirmed(e: android.view.MotionEvent): Boolean {
                    togglePlayPause()
                    return true
                }

                // 🔥🔥 双击：点赞 + 动画
                override fun onDoubleTap(e: android.view.MotionEvent): Boolean {
                    // 1. 触发点赞逻辑
                    if (currentItem != null && !currentItem!!.isLiked) {
                        // 只有未点赞时才触发逻辑，或者双击总是触发动画
                        currentItem!!.isLiked = true
                        updateLikeButton(true, animate = true)

                        // 简单的数字 +1
                        currentItem!!.likeCount++
                        binding.tvLikeCount.text = currentItem!!.likeCount.toString()
                    } else {
                        // 如果已经赞了，双击通常只播动画，不取消赞 (符合抖音逻辑)
                        updateLikeButton(true, animate = true)
                    }

                    // 2. 在点击位置播放大爱心动画
                    showHeartAnimation(e.x, e.y)
                    return true
                }
            }
        )

        fun bind(item: VideoItem, isFirstItem: Boolean, onReady: () -> Unit,onAvatarClick: (position: Int) -> Unit,
                 currentPosition: Int) {
            currentItem = item
            binding.tvTitle.text = item.title
            binding.tvAuthor.text = item.author
            binding.tvLikeCount.text = item.likeCount.toString()
            binding.tvCollectCount.text = item.collectCount.toString()
            binding.tvCommentCount.text = item.commentCount.toString()
            binding.ivPlayStatus.visibility = android.view.View.GONE


            val avatarToLoad = item.localAvatarUri ?: item.coverUrl
            com.bumptech.glide.Glide.with(binding.root.context)
                .load(avatarToLoad)
                .circleCrop() // 确保是圆的
                .into(binding.ivAvatar)

            // 🔥 设置头像点击事件
            binding.ivAvatar.setOnClickListener {
                onAvatarClick(currentPosition)
            }

            updateLikeButton(item.isLiked, animate = false)
            binding.ivLike.setOnClickListener {
                item.isLiked = !item.isLiked
                updateLikeButton(item.isLiked, animate = true)
                binding.tvLikeCount.text = if (item.isLiked) "${item.likeCount + 1}" else "${item.likeCount}"
            }

            binding.layoutMusicDisc.rotation = 0f

            if (rotateAnimator == null) {
                // 创建一个旋转 0 -> 360 度的动画
                rotateAnimator = android.animation.ObjectAnimator.ofFloat(
                    binding.layoutMusicDisc,
                    "rotation",
                    0f,
                    360f
                )
                rotateAnimator?.duration = 4000 // 4秒转一圈
                rotateAnimator?.repeatCount = android.animation.ObjectAnimator.INFINITE // 无限循环
                rotateAnimator?.interpolator = android.view.animation.LinearInterpolator() // 匀速
            }

            // 确保动画是停止状态
            rotateAnimator?.cancel()

            // 2. 初始化收藏按钮状态
            updateCollectButton(item.isCollected, animate = false)
            binding.ivCollect.setOnClickListener {
                item.isCollected = !item.isCollected
                updateCollectButton(item.isCollected, animate = true)
                binding.tvCollectCount.text = if (item.isCollected) "${item.collectCount + 1}" else "${item.collectCount}"
            }

            binding.ivComment.setOnClickListener {
                showCommentDialog(binding.root.context, item)
            }

            binding.ivShare.setOnClickListener {
                android.widget.Toast.makeText(binding.root.context, "开始分享", android.widget.Toast.LENGTH_SHORT).show()
            }

            currentUrl = item.videoUrl
            binding.ivCoverTransition.transitionName = item.videoUrl
            binding.ivCoverTransition.visibility = android.view.View.VISIBLE

            com.bumptech.glide.Glide.with(binding.root.context)
                .load(item.coverUrl)
                .into(binding.ivCoverTransition)

            if (isFirstItem) {
                binding.playerView.viewTreeObserver.addOnPreDrawListener(
                    object : android.view.ViewTreeObserver.OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            binding.playerView.viewTreeObserver.removeOnPreDrawListener(this)
                            onReady()
                            return true
                        }
                    }
                )
            }
        }

        fun play() {
            if (currentUrl == null) return

            // 1. 创建播放器
            if (player == null) {
                player = ExoPlayer.Builder(binding.root.context).build()
            }

            // 2. 绑定视图 (必须有)
            binding.playerView.player = player

            // 3. 🔥🔥🔥 核心修改：添加监听器来隐藏封面图
            player?.addListener(object : androidx.media3.common.Player.Listener {
                // 时机 A：视频第一帧渲染好了 -> 完美隐藏
                override fun onRenderedFirstFrame() {
                    hideCoverImage()
                }

                // 时机 B：状态变成“播放中”了 -> 兜底隐藏
                // (防止有时候第一帧回调没触发，导致画面一直被遮住)
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        // 延迟一小会儿强制移除，确保万无一失
                        binding.ivCoverTransition.postDelayed({
                            hideCoverImage()
                        }, 500)
                    }
                }
            })

            // 4. 准备资源
            val mediaItem = MediaItem.fromUri(currentUrl!!)
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.play()

            binding.ivPlayStatus.visibility = android.view.View.GONE

            // 5. 🔥 修改触摸监听：只保留这一个！
            // 删掉原来的 setOnClickListener，逻辑全部交给 gestureDetector 处理
            binding.root.setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                // 必须返回 true，表示“我接收了这个事件”，
                // 否则 gestureDetector 可能收不到后续的“抬起”动作，也就无法判断单击/双击
                true
            }

            if (rotateAnimator?.isPaused == true) {
                rotateAnimator?.resume() // 如果是暂停状态，继续转
            } else {
                rotateAnimator?.start()  // 如果是停止状态，重新转
            }
        }

        // 🔧 辅助方法：渐隐消失封面图
        private fun hideCoverImage() {
            if (binding.ivCoverTransition.visibility == android.view.View.VISIBLE) {
                binding.ivCoverTransition.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        binding.ivCoverTransition.visibility = android.view.View.GONE
                    }
                    .start()
            }
        }

        fun pause() {
            player?.pause()
            rotateAnimator?.pause()
            binding.ivPlayStatus.visibility = android.view.View.VISIBLE
            binding.ivPlayStatus.alpha = 1f
        }

        private fun togglePlayPause() {
            val player = this.player ?: return

            if (player.isPlaying) {
                // 🛑 暂停逻辑：直接调用我们封装好的 pause() 方法
                // 这样既暂停了视频，又暂停了转盘动画
                pause()
            } else {
                // ▶️ 播放逻辑
                player.play()

                // 🔥🔥🔥 核心修复：同步恢复转盘动画
                if (rotateAnimator?.isPaused == true) {
                    rotateAnimator?.resume()
                } else {
                    rotateAnimator?.start()
                }

                // 隐藏暂停图标的动画 (保持不变)
                binding.ivPlayStatus.animate()
                    .alpha(0f)
                    .scaleX(1.5f)
                    .scaleY(1.5f)
                    .setDuration(200)
                    .withEndAction {
                        binding.ivPlayStatus.visibility = android.view.View.GONE
                        binding.ivPlayStatus.alpha = 1f
                        binding.ivPlayStatus.scaleX = 1f
                        binding.ivPlayStatus.scaleY = 1f
                    }
                    .start()
            }
        }

        fun release() {
            player?.release()
            player = null
            binding.playerView.player = null
            binding.root.setOnClickListener(null)
            rotateAnimator?.cancel()
        }

        private fun updateLikeButton(isLiked: Boolean, animate: Boolean) {
            // 1. 设置颜色 (红色 vs 白色)
            val color = if (isLiked) 0xFFFF4081.toInt() else 0xFFFFFFFF.toInt()
            binding.ivLike.setColorFilter(color)

            // 2. 执行 Q 弹动画
            if (animate) {
                playBounceAnimation(binding.ivLike)
            }
        }

        private fun updateCollectButton(isCollected: Boolean, animate: Boolean) {
            // 1. 设置颜色 (黄色 vs 白色)
            val color = if (isCollected) 0xFFFFC107.toInt() else 0xFFFFFFFF.toInt()
            binding.ivCollect.setColorFilter(color)

            // 2. 执行 Q 弹动画
            if (animate) {
                playBounceAnimation(binding.ivCollect)
            }
        }

        // 通用的“缩放回弹”动画 helper
        private fun playBounceAnimation(view: android.view.View) {
            view.animate()
                .scaleX(1.2f) // 放大到 1.2 倍
                .scaleY(1.2f)
                .setDuration(150) // 耗时 150ms
                .withEndAction {
                    // 动画结束后，缩放回原大小
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start()
                }
                .start()
        }

        private fun showCommentDialog(context: android.content.Context, item: VideoItem) {
            // 需要 FragmentManager，所以 context 必须是 FragmentActivity
            val activity = context as? androidx.fragment.app.FragmentActivity ?: return

            val dialog = CommentBottomSheet(item) { newCount ->
                // 回调：当评论数变化时，更新界面
                binding.tvCommentCount.text = newCount.toString()
            }
            dialog.show(activity.supportFragmentManager, "CommentDialog")
        }

        private fun showHeartAnimation(x: Float, y: Float) {
            val heartView = binding.ivDoubleTapHeart

            // 1. 移动到点击位置
            // 因为 View 是居中的，我们要根据点击坐标计算偏移量
            // 简单算法：把 View 的中心点移到 (x, y)
            heartView.translationX = x - (binding.root.width / 2)
            heartView.translationY = y - (binding.root.height / 2)

            // 2. 准备动画状态 (缩放 0 -> 1, 透明度 1, 旋转随机角度)
            heartView.visibility = android.view.View.VISIBLE
            heartView.alpha = 1f
            heartView.scaleX = 0f
            heartView.scaleY = 0f
            heartView.rotation = (-30..30).random().toFloat() // 随机歪一点，更有趣

            // 3. 执行动画：弹出 -> 停顿 -> 淡出
            heartView.animate()
                .scaleX(1.2f).scaleY(1.2f) // 放大一点点
                .setDuration(200)
                .withEndAction {
                    // 停顿一下再消失
                    heartView.animate()
                        .scaleX(0.8f).scaleY(0.8f) // 缩小
                        .alpha(0f) // 淡出
                        .translationYBy(-100f) // 稍微往上飘一点
                        .setDuration(400)
                        .withEndAction {
                            heartView.visibility = android.view.View.GONE
                            // 复位
                            heartView.translationX = 0f
                            heartView.translationY = 0f
                        }
                        .start()
                }
                .start()
        }
    }
}