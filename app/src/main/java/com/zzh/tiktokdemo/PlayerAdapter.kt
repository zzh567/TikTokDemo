package com.zzh.tiktokdemo

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
    private val onVideoReady: () -> Unit
) : RecyclerView.Adapter<PlayerAdapter.VideoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoPlayerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videoList[position], position == startPosition, onVideoReady)
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

        fun bind(item: VideoItem, isFirstItem: Boolean, onReady: () -> Unit) {
            binding.tvTitle.text = item.title
            binding.tvAuthor.text = item.author
            binding.tvLikeCount.text = item.likeCount.toString()
            binding.ivPlayStatus.visibility = android.view.View.GONE

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
            if (player == null) {
                player = ExoPlayer.Builder(binding.root.context).build()
            }
            binding.playerView.player = player

            player?.addListener(object : androidx.media3.common.Player.Listener {
                override fun onRenderedFirstFrame() {
                    binding.ivCoverTransition.animate().alpha(0f).setDuration(200)
                        .withEndAction { binding.ivCoverTransition.visibility = android.view.View.GONE }
                        .start()
                }
            })

            val mediaItem = MediaItem.fromUri(currentUrl!!)
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.play()

            binding.root.setOnClickListener { togglePlayPause() }
        }

        fun pause() {
            player?.pause()
            binding.ivPlayStatus.visibility = android.view.View.VISIBLE
            binding.ivPlayStatus.alpha = 1f
        }

        private fun togglePlayPause() {
            val player = this.player ?: return
            if (player.isPlaying) {
                pause() // 复用上面的 pause 方法
            } else {
                player.play()
                binding.ivPlayStatus.animate().alpha(0f).scaleX(1.5f).scaleY(1.5f).setDuration(200)
                    .withEndAction {
                        binding.ivPlayStatus.visibility = android.view.View.GONE
                        binding.ivPlayStatus.alpha = 1f
                        binding.ivPlayStatus.scaleX = 1f
                        binding.ivPlayStatus.scaleY = 1f
                    }.start()
            }
        }

        fun release() {
            player?.release()
            player = null
            binding.playerView.player = null
            binding.root.setOnClickListener(null)
            // 🔥 删除：removeObserver 也不需要了
        }
    }
}