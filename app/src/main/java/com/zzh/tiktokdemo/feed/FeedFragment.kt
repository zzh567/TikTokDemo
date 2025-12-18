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

    // 懒加载初始化 ViewModel
    private val viewModel: FeedViewModel by viewModels()

    private val feedAdapter = FeedAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFeedBinding.bind(view)

        setupRecyclerView()
        setupRefreshLayout()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.feedRecyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = feedAdapter
            itemAnimator = null
        }
    }

    private fun setupRefreshLayout() {
        // 监听下拉动作
        binding.refreshLayout.setOnRefreshListener {
            // 告诉 ViewModel：用户想刷新，请重新去网络拉数据！
            viewModel.fetchVideoList()
        }
    }

    // NEW: 核心观察逻辑
    private fun observeViewModel() {
        viewModel.videoList.observe(viewLifecycleOwner) { videos ->
            if (videos != null && videos.isNotEmpty()) {
                feedAdapter.submitList(videos) {
                    val newLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    binding.feedRecyclerView.layoutManager = newLayoutManager
                }
                binding.refreshLayout.finishRefresh()
            } else {
                binding.refreshLayout.finishRefresh(false)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}