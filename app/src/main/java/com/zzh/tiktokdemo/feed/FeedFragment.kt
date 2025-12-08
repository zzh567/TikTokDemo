package com.zzh.tiktokdemo.feed

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // 1. 需要这个扩展函数
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.zzh.tiktokdemo.R
import com.zzh.tiktokdemo.databinding.FragmentFeedBinding

class FeedFragment : Fragment(R.layout.fragment_feed) {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    // NEW: 2. 懒加载初始化 ViewModel
    // 这种写法会让系统自动帮你创建 ViewModel 实例
    private val viewModel: FeedViewModel by viewModels()

    private val feedAdapter = FeedAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFeedBinding.bind(view)

        setupRecyclerView()
        observeViewModel() // NEW: 3. 开始观察数据
    }

    private fun setupRecyclerView() {
        binding.feedRecyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = feedAdapter
            itemAnimator = null
        }
    }

    // NEW: 核心观察逻辑
    private fun observeViewModel() {
        // 观察视频列表数据
        viewModel.videoList.observe(viewLifecycleOwner) { videos ->
            // 当数据发生变化时，这个 lambda 会自动执行
            if (videos != null && videos.isNotEmpty()) {
                // 将数据提交给 Adapter，Adapter 会自动计算差异并刷新 UI
                feedAdapter.submitList(videos)
            }
        }

        // 观察 Loading 状态 (可选：如果你在 XML 里加了 ProgressBar)
        /*
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        */
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}