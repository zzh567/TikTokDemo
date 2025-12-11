package com.zzh.tiktokdemo.feed // 确保修改为你自己的包名

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.zzh.tiktokdemo.R
import com.zzh.tiktokdemo.vedioclass.VideoItem
import com.bumptech.glide.Glide
import com.zzh.tiktokdemo.PlayerActivity
import com.zzh.tiktokdemo.databinding.ItemVideoFeedBinding
import android.app.Activity
import androidx.core.app.ActivityOptionsCompat

// 1. 实现 DiffUtil.ItemCallback，高效对比新旧列表
class FeedDiffCallback : DiffUtil.ItemCallback<VideoItem>() {
    override fun areItemsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
        // 判断两个 Item 是否代表同一个实体（通常比较 ID）
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
        // 判断 Item 的内容是否发生变化
        return oldItem == newItem // Data Class 自动实现了 equals()
    }
}


class FeedAdapter : ListAdapter<VideoItem, FeedAdapter.FeedViewHolder>(FeedDiffCallback()) {

    // 2. ViewHolder (持有视图引用)
    inner class FeedViewHolder(private val binding: ItemVideoFeedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VideoItem) {
            binding.tvTitle.text = item.title
            binding.tvLikes.text = item.likeCount.toString()
            binding.tvAuthor.text = item.author
            binding.ivCover.transitionName = item.videoUrl
            if (item.isLiked) {
                // 已点赞：设置为粉红色
                binding.ivLikeIcon.setColorFilter(0xFFFF4081.toInt())
            } else {
                // 未点赞：设置为灰色 (根据你的背景色，如果是白底通常用灰色 #888888)
                binding.ivLikeIcon.setColorFilter(0xFF888888.toInt())
            }

            val params = binding.ivCover.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams

            // 2. 关键：强制设置高度为 0 (MATCH_CONSTRAINT)
            // 只有高度设为 0，下面的 dimensionRatio 才会生效！
            params.height = 0

            // 3. 设置宽高比 (格式: "H,宽:高")
            // "H" 代表以宽度为基准，根据 JSON 里的宽高动态计算高度
            params.dimensionRatio = "H,${item.width}:${item.height}"

            // 4. 把修改后的参数应用回去
            binding.ivCover.layoutParams = params

            Glide.with(itemView.context)
                .load(item.coverUrl)
                .placeholder(R.drawable.ic_placeholder)
                .into(binding.ivCover)

            // 关键：点击事件 (Day 3 的入口)
            binding.root.setOnClickListener {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    itemView.context as Activity,
                    binding.ivCover,
                    item.videoUrl
                )
                PlayerActivity.start(itemView.context, currentList, position, options.toBundle())
            }
        }
    }

    // 3. 创建 ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val binding = ItemVideoFeedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FeedViewHolder(binding)
    }

    // 4. 绑定数据
    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}