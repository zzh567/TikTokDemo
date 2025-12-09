package com.zzh.tiktokdemo

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.zzh.tiktokdemo.databinding.ActivityMainBinding
import com.zzh.tiktokdemo.feed.HomeFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // 定义两个 Fragment 实例，避免重复创建
    private val homeFragment by lazy { HomeFragment() }
    // private val meFragment by lazy { MeFragment() } // 还没写，先注释

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 初始化：默认加载首页，并设置按钮样式
        switchFragment(homeFragment)
        updateBottomTabUI(isHome = true)

        // 2. 点击“首页”
        binding.btnHome.setOnClickListener {
            switchFragment(homeFragment)
            updateBottomTabUI(isHome = true)
        }

        // 3. 点击“我的”
        binding.btnMe.setOnClickListener {
            // switchFragment(meFragment) // 暂时还没写 MeFragment
            updateBottomTabUI(isHome = false)
        }
    }

    /**
     * 核心：手动控制 UI 样式的变化
     * 这种简单的 if-else 比改 XML Style 简单一万倍
     */
    private fun updateBottomTabUI(isHome: Boolean) {
        if (isHome) {
            // 选中首页：首页变白变粗，我的变灰变细
            binding.btnHome.setTextColor(Color.WHITE)
            binding.btnHome.typeface = Typeface.DEFAULT_BOLD

            binding.btnMe.setTextColor(Color.parseColor("#99FFFFFF"))
            binding.btnMe.typeface = Typeface.DEFAULT
        } else {
            // 选中我的：反之
            binding.btnHome.setTextColor(Color.parseColor("#99FFFFFF"))
            binding.btnHome.typeface = Typeface.DEFAULT

            binding.btnMe.setTextColor(Color.WHITE)
            binding.btnMe.typeface = Typeface.DEFAULT_BOLD
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container, fragment)
            .commit()
    }
}