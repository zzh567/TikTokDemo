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
        if (savedInstanceState == null) {
            // 只有在第一次启动（非重建）时，才手动添加 Fragment
            switchFragment(homeFragment)
            updateBottomTabUI(isHome = true)
        } else {
            // 如果是旋转屏幕回来的，系统已经恢复了 Fragment，
            // 我们只需要根据当前显示的 Fragment 恢复底部按钮的状态即可。
            // 可配合 ViewModel 保存当前选中的 tab index
        }

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

    private fun updateBottomTabUI(isHome: Boolean) {
        if (isHome) {
            // 选中首页
            binding.btnHome.setTextColor(Color.WHITE)
            binding.btnHome.typeface = Typeface.DEFAULT_BOLD

            binding.btnMe.setTextColor(Color.parseColor("#99FFFFFF"))
            binding.btnMe.typeface = Typeface.DEFAULT
        } else {
            // 选中我的
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