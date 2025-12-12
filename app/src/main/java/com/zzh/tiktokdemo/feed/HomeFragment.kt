package com.zzh.tiktokdemo.feed

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.zzh.tiktokdemo.R
import com.zzh.tiktokdemo.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        // 1. 设置 ViewPager 的 Adapter
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 4

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> FeedFragment()
                    1 -> FeedFragment()
                    2 -> CityFragment()
                    3 -> StoreFragment()
                    else -> FeedFragment()
                }
            }
        }

        // 2. 将 TabLayout 和 ViewPager2 绑定 (实现联动)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "推荐"
                1 -> "关注"
                2 -> "同城"
                3 -> "商城"
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}