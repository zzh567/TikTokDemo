package com.zzh.tiktokdemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.zzh.tiktokdemo.databinding.ActivityPlayerBinding
import com.zzh.tiktokdemo.vedioclass.VideoItem

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var videoList: ArrayList<VideoItem> = ArrayList()
    private var startPosition: Int = 0

    private var currentPlayingHolder: PlayerAdapter.VideoViewHolder? = null

    companion object {
        private const val EXTRA_LIST = "extra_list"
        private const val EXTRA_POS = "extra_pos"

        // 封装启动方法，让调用者更方便
        fun start(context: Context, list: List<VideoItem>, position: Int, bundle: Bundle? = null) {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putParcelableArrayListExtra(EXTRA_LIST, ArrayList(list))
            intent.putExtra(EXTRA_POS, position)
            // 启动时带上动画参数
            context.startActivity(intent, bundle)
        }
    }

    private val viewModel: PlayerViewModel by viewModels()

    // 保存 Adapter 引用，方便后面调用 addData
    private lateinit var adapter: PlayerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportPostponeEnterTransition()

        // 1. 获取 Intent 数据
        videoList = intent.getParcelableArrayListExtra(EXTRA_LIST) ?: ArrayList()
        startPosition = intent.getIntExtra(EXTRA_POS, 0)

        initViewPager()
        setupSmartRefresh() // 🔥 2. 配置刷新
        observeViewModel()  // 🔥 3. 观察数据
    }

    private fun initViewPager() {
        binding.viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL

        adapter = PlayerAdapter(videoList, startPosition, {
            supportStartPostponedEnterTransition()
        })

        binding.viewPager.adapter = adapter

        // 1. 设置默认位置 (不要平滑滚动)
        binding.viewPager.setCurrentItem(startPosition, false)

        // 🔥 核心修改 2：监听翻页，手动控制播放
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 当页面选中时，执行播放逻辑
                playVideoAt(position)
            }
        })

        // 🔥 补充：因为第一次进入时不会触发 onPageSelected，需要手动触发一次
        binding.viewPager.post {
            playVideoAt(startPosition)
        }
    }
    private fun playVideoAt(position: Int) {
        // ViewPager2 内部其实就是一个 RecyclerView
        val recyclerView = binding.viewPager.getChildAt(0) as RecyclerView

        // 遍历当前屏幕上所有“活着”的 ViewHolder (通常也就 2-3 个)
        val childCount = recyclerView.childCount
        for (i in 0 until childCount) {
            val view = recyclerView.getChildAt(i)
            val viewHolder = recyclerView.getChildViewHolder(view)

            if (viewHolder is PlayerAdapter.VideoViewHolder) {
                // 判断：是当前选中的吗？
                if (viewHolder.bindingAdapterPosition == position) {
                    // ✅ 是主角 -> 播放
                    viewHolder.play()
                    currentPlayingHolder = viewHolder
                } else {
                    // ❌ 是配角 (上一个或下一个) -> 停止/释放
                    viewHolder.release()
                }
            }
        }
    }
    private fun setupSmartRefresh() {
        // 下拉刷新
        binding.refreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
        // 上拉加载
        binding.refreshLayout.setOnLoadMoreListener {
            viewModel.loadMore()
        }
    }

    private fun observeViewModel() {
        // 监听数据变化
        viewModel.newVideoList.observe(this) { newVideos ->
            if (binding.refreshLayout.isRefreshing) {
                // 如果是正在刷新 -> 重置列表
                adapter.refreshData(newVideos)
                binding.refreshLayout.finishRefresh()
                // 刷新后可能需要重置播放位置到 0
                binding.viewPager.setCurrentItem(0, false)
            } else {
                // 如果是加载更多 -> 追加列表
                adapter.addData(newVideos)
                binding.refreshLayout.finishLoadMore()
            }
        }

        // 监听失败情况
        viewModel.loadState.observe(this) { success ->
            if (!success) {
                binding.refreshLayout.finishRefresh(false)
                binding.refreshLayout.finishLoadMore(false)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // 直接命令存好的 holder 暂停
        currentPlayingHolder?.pause()
    }

    override fun onResume() {
        super.onResume()
        currentPlayingHolder?.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 直接命令存好的 holder 释放
        currentPlayingHolder?.release()
        currentPlayingHolder = null // 避免内存泄漏
    }
}