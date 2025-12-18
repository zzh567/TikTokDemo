package com.zzh.tiktokdemo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.media3.common.util.UnstableApi

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.yalantis.ucrop.UCrop
import com.zzh.tiktokdemo.databinding.ActivityPlayerBinding
import com.zzh.tiktokdemo.vedioclass.VideoItem
import java.io.File
import androidx.media3.exoplayer.ExoPlayer

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

    private val globalPlayer by lazy {
        ExoPlayer.Builder(this).build()
    }

    private val viewModel: PlayerViewModel by viewModels()

    // 保存 Adapter 引用，方便后面调用 addData
    private lateinit var adapter: PlayerAdapter

    // 记录当前正在修改头像的那一项的索引
    private var currentChangingPosition = -1
    // 拍照时照片的临时存 Uri
    private lateinit var photoUri: Uri

    // 🔥 1. 定义图库启动器
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { startCrop(it) } // 拿到图片，去裁剪
    }

    // 🔥 2. 定义相机启动器
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            startCrop(photoUri!!) // 拍照成功，去裁剪
        }
    }

    // 🔥 3. 定义裁剪启动器 (uCrop)
    private val cropLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val resultUri = UCrop.getOutput(result.data!!)
            handleCropResult(resultUri)
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            Toast.makeText(this, "裁剪失败", Toast.LENGTH_SHORT).show()
        }
    }

    // 🔥 4. 定义权限请求启动器 (简单处理，为了演示核心流程)
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //初始化视频缓存
        VideoCache.init(this)

        supportPostponeEnterTransition()

        // 1. 获取 Intent 数据
        videoList = intent.getParcelableArrayListExtra(EXTRA_LIST) ?: ArrayList()
        startPosition = intent.getIntExtra(EXTRA_POS, 0)

        initViewPager()
        setupSmartRefresh() // 🔥 2. 配置刷新
        observeViewModel()  // 🔥 3. 观察数据
        binding.floatingAiBall.setOnClickListener {
            showAiChatDialog()
        }
    }
    private fun showAiChatDialog() {
        // 暂停视频 (可选，看需求)
        globalPlayer.pause()
        currentPlayingHolder?.pauseAnimation()

        val dialog = AiChatDialogFragment()
        dialog.show(supportFragmentManager, "AiChatDialog")
    }

    private fun initViewPager() {
        binding.viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL

        adapter = PlayerAdapter(
            videoList, startPosition,
            {
                supportStartPostponedEnterTransition()
            },
            { position -> showAvatarSelectionDialog(position) },
        )

        binding.viewPager.adapter = adapter

        binding.viewPager.offscreenPageLimit = 1

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

    @OptIn(UnstableApi::class)
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
                    // 1. 先让上一个视频（如果有）把播放器交出来
                    currentPlayingHolder?.detachPlayer()

                    // 2. 让当前的 holder 接管播放器
                    if (viewHolder is PlayerAdapter.VideoViewHolder) {
                        viewHolder.attachPlayer(globalPlayer, videoList[position].videoUrl)
                        currentPlayingHolder = viewHolder
                    }

                } else {
                    // ❌ 是配角 (上一个或下一个) -> 停止/释放
                    viewHolder.detachPlayer()
                }
            }
        }
        if (position + 1 < videoList.size) {
            val nextVideoUrl = videoList[position + 1].videoUrl
            VideoCache.preLoadNextVideo(nextVideoUrl)
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
                // 1. 刷新数据
                adapter.refreshData(newVideos)
                binding.refreshLayout.finishRefresh()

                // 2. 重置位置到 0
                binding.viewPager.setCurrentItem(0, false)

                // ✅✅✅ 修复方案：手动触发第 0 个视频的播放
                // 使用 post 是为了等待 RecyclerView 布局刷新完成，确保能找到 ViewHolder
                binding.viewPager.post {
                    playVideoAt(0)
                }

            } else {
                // 加载更多 (Load More) 的逻辑通常是正常的
                // 因为加载更多后，用户需要滑到下一个位置，这会自动触发 onPageSelected
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
        globalPlayer.pause()
        currentPlayingHolder?.pauseAnimation()
    }

    override fun onResume() {
        super.onResume()
        globalPlayer.play()
        currentPlayingHolder?.resumeAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 直接命令存好的 holder 释放
        currentPlayingHolder = null // 避免内存泄漏
        globalPlayer.release()
    }

    // 更换头像
    // 步骤 A: 显示选择对话框
    private fun showAvatarSelectionDialog(position: Int) {
        // 🔥🔥🔥 修复 Bug 1: 弹窗时主动暂停视频
        // 因为 Dialog 不会触发 onPause，所以我们得手动停
        globalPlayer.pause()
        currentPlayingHolder?.pauseAnimation()

        currentChangingPosition = position
        val options = arrayOf("拍照", "从相册选择")
        AlertDialog.Builder(this)
            .setTitle("更换头像")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> galleryLauncher.launch("image/*")
                }
            }
            .setOnCancelListener {
                // 可选：如果用户取消弹窗，恢复播放
                globalPlayer.play()
                currentPlayingHolder?.resumeAnimation()
            }
            .show()
    }

    // 步骤 B: 检查权限并打开相机
    private fun checkCameraPermissionAndOpen() {
        // 简单检查相机权限
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // 步骤 C: 真正打开相机
    private fun openCamera() {
        // 1. 创建一个临时文件用来存照片
        val photoFile = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "temp_avatar_${System.currentTimeMillis()}.jpg"
        )
        // 2. 通过 FileProvider 获取安全的 Uri
        photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        // 3. 启动相机
        cameraLauncher.launch(photoUri)
    }

    // 步骤 D: 开始裁剪 (uCrop 核心配置)
    private fun startCrop(sourceUri: Uri) {
        // 1. 定义裁剪后文件的保存位置 (缓存目录)
        val destinationFileName = "cropped_avatar_${System.currentTimeMillis()}.jpg"
        val destinationUri = Uri.fromFile(File(cacheDir, destinationFileName))

        // 2. 配置 uCrop 选项
        val options = UCrop.Options().apply {
            setCircleDimmedLayer(true) // 🔥 关键：设置为圆形遮罩层！
            setShowCropFrame(false)    // 隐藏矩形边框
            setShowCropGrid(false)     // 隐藏网格
            setCompressionQuality(80)  // 压缩质量
            // 可以设置主题色...
            // setToolbarColor(getColor(R.color.colorPrimary))
        }

        // 3. 构建 Intent 并启动
        val intent = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f) // 强制 1:1 方形比例
            .withOptions(options)
            .getIntent(this)

        cropLauncher.launch(intent)
    }

    // 步骤 E: 处理裁剪结果，更新 UI
    private fun handleCropResult(resultUri: Uri?) {
        if (resultUri != null && currentChangingPosition != -1) {
            // 1. 更新数据源
            videoList[currentChangingPosition].localAvatarUri = resultUri.toString()

            // 🔥🔥🔥 修复 Bug 2: 使用 Payload 进行局部刷新
            // 传一个 "UPDATE_AVATAR" 字符串，告诉 Adapter 别动播放器，只换头像
            adapter.notifyItemChanged(currentChangingPosition, "UPDATE_AVATAR")

            Toast.makeText(this, "头像更换成功", Toast.LENGTH_SHORT).show()
        }
    }
}