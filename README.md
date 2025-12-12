## 1. 项目启动：

1. 在GoServer目录运行go run server.go在本机8080端口启动本地视频服务器
2. 在Android Studio中打开工程并运行即可成功运行demo。

说明：由于demo运行在Android Studio的虚拟机中，所以代码里是写死从http://10.0.2.2:8080/ 获取数据的，若将apk安装到真机或其他模拟器则无法收到本地Go服务器提供的视频数据。



## 2. 项目介绍：

**TikTokDemo** 是一个基于 Android 原生技术栈（Kotlin + Jetpack）开发的高仿抖音短视频应用。本项目旨在复刻抖音的核心交互体验。项目采用现代化的 **MVVM 架构**，实现了从双列瀑布流到全屏沉浸式播放的丝滑切换，并集成了如下业务模块。

| 功能                                  |                           实现方法                           | 完成时间 |
| ------------------------------------- | :----------------------------------------------------------: | -------- |
| 抓取和mock一定的图片，文本和视频      |    下载视频并使用ffmpeg批量截取封面获取封面宽高并制作json    | 12月7日  |
| 双列外流的UI布局                      |                          Layout实现                          | 12月8日  |
| 点击封面进入视频内流                  |                       监听点击事件跳转                       | 12月9日  |
| 进入视频内流有画面放大的转场动画      | 在视频卡片页和视频内流页通过相同的封面实现共享元素转场，重载PlayerActivity的onCreate函数，在函数中暂停入场动画，同时传入启动入场动画的回调函数给PlayerAdapter，在Adapter在视频内流页面加载完成封面图后回调启动动画实现衔接。最后当Exoplayer组件把视频的第一帧播放完成时将封面图设置为不可见，完成全部转场过程。 | 12月10日 |
| 每个卡片页面高度自适应布局            | 服务器为每个视频的首帧截取了封面，计算封面图的宽高数据写入json中，卡片布局显示时首先通过获取的宽高数据设置到dimensionRatio即宽高比属性中优先进行占位再加载图片，这样就能使页面自适应布局并且不会闪动。 | 12月8日  |
| 下拉刷新，上拉加载更新数据            | 1.刷新时数据不重新排布：在服务器后端逻辑中提取json内容进行打乱，在每次被请求时返回打乱的json数据实现刷新后视频位置变化的效果。2.刷新后视频item顶部未对齐：在每次监听到LiveData更新时，创建一个新的Manager给feedRecyclerView重新绑定实现刷新页面，虽然引入新的开销但是解决了刷新后item顶部不对齐，或者使用回滚对齐时的屏幕闪烁问题。 | 12月9日  |
| 顶部和底部bar实现，支持切换           | 底部bar通过首页给底部按钮绑定切换Fragment的函数来实现。顶部bar和显示页面结合，使用ViewPager2来显示页面内容，TabLayout展示顶部标签，创建继承自FragmentStateAdapter(this)的内部类分发页面和顶部标签名 | 12月8日  |
| 在双列外流左右滑动，跳转不同的顶部bar | 顶部bar和显示页面结合，使用ViewPager2来显示页面内容，TabLayout展示顶部标签，创建继承自FragmentStateAdapter(this)的内部类分发页面和顶部标签名 | 12月8日  |
| 内流页面布局                          |                          Layout实现                          | 12月9日  |
| 点击暂停、播放                        | 使用GestureDetector接管Item的触摸事件，重写onTouchEvent实现单击暂停逻辑。 | 12月9日  |
| 手指上下移动、切换                    | 基于ViewPager2实现全屏视频的纵向滑动翻页效果。在PlayerAdapter中集成 ExoPlayer，通过registerOnPageChangeCallback监听页面选中状态。仅对当前选中的Item初始化播放器并自动播放，页面滑出时立即释放播放器资源 | 12月9日  |
| 双击点赞动画                          | 使用GestureDetector接管Item的触摸事件，重写onDoubleTap实现双击点赞逻辑。在双击坐标处动态添加 ImageView，利用 ViewPropertyAnimator 组合缩放、透明度渐变与位移属性，实现爱心弹出的动画效果。同时对底部点赞按钮应用缩放插值器动画，增强用户操作的视觉反馈。 | 12月10日 |
| 音乐转盘和动画                        | 利用ObjectAnimator对音乐转盘执行无限循环旋转动画，并设置LinearInterpolator插值器保证旋转匀速。在VideoViewHolder中监听视频播放状态，当视频播放/暂停时同步调用动画的resume()和pause()方法，确保视觉动效与视频流状态严格同步。 | 12月11日 |
| 下拉刷新，上拉加载更新数据            | 集成 SmartRefreshLayout 控件，通过 setOnRefreshListener 和 setOnLoadMoreListener 分别监听用户的下拉与上拉手势。在 ViewModel 中发起异步数据请求，通过LiveData观察数据状态，分别调用Adapter的refreshData（清空并重置列表）和addData（尾部追加数据）方法，实现无缝的数据分页更新。 | 12月9日  |
| 支持头像更换                          | 使用 ActivityResultContracts 注册相机和图库的启动器，替代传统的 startActivityForResult 以简化权限与回调处理。集成 UCrop 库对选取的图片进行圆形裁剪。在更新 UI 时，利用 RecyclerView 的 notifyItemChanged(position, payload) 机制传递 "UPDATE_AVATAR" 标识，仅局部刷新头像控件，避免了因刷新整个 Item 导致视频播放器重建或中断播放的问题。 | 12月10日 |
| 评论UI布局                            |                          Layout实现                          | 12月10日 |
| 评论页面高度自适应布局                | 基于 Material Components 的 BottomSheetDialogFragment 实现，利用其默认的 design_bottom_sheet 行为控制弹窗高度。布局根节点设为 match_parent，内部RecyclerView使用LinearLayoutManager占据剩余空间 | 12月10日 |
| 支持发布新评论并展示最顶部            | 在用户点击发送后，构造包含当前时间戳和用户信息的 Comment 对象。在 Adapter 内部将新数据插入到数据源列表的索引 0 位置（头部），调用 notifyItemInserted(0) 触发原生插入动画，随后立即调用 scrollToPosition(0) 强制列表滚动至最顶部，确保用户能即时看到最新发布的评论。 | 12月10日 |
| 制作一个悬浮球UI，可在页面拖动        | 自定义继承自 AppCompatImageView 的 View (DraggableFloatingView)，重写 onTouchEvent 方法接管触摸事件。在 ACTION_MOVE 事件中计算手指滑动的 dx 和 dy 偏移量，实时修改 View 的 translationX 和 translationY 属性实现跟随拖拽，并通过设置 touchSlop 阈值有效区分用户的点击操作与拖拽行为，防止误触。 | 12月10日 |
| 点击悬浮球打开聊天页面，和与AI对话    |                     未接入真实api仅模拟                      | 12月10日 |

## 3. 技术选型

| **类别**     | **技术/库**                         | **说明**                              |
| ------------ | ----------------------------------- | ------------------------------------- |
| **语言**     | Kotlin                              | 全项目 100% Kotlin 代码               |
| **架构模式** | MVVM                                | Model-View-ViewModel                  |
| **UI 组件**  | ViewPager2, RecyclerView, Fragment  | 核心容器组件                          |
| **异步处理** | Coroutines (协程) + LiveData        | 替代传统 Thread/Handler，处理异步任务 |
| **网络请求** | Retrofit2 + OkHttp3                 | RESTful API 请求封装                  |
| **图片加载** | Glide                               | 高性能图片缓存与加载                  |
| **视频播放** | AndroidX Media3 (ExoPlayer)         | 专业级视频播放引擎                    |
| **图片裁剪** | UCrop                               | 头像裁剪功能                          |
| **UI 适配**  | ConstraintLayout, CoordinatorLayout | 复杂布局与联动效果                    |

## 4. 系统架构与类图

本项目采用单 Activity 多 Fragment 架构（MainActivity）作为主入口，视频播放页采用独立 Activity（PlayerActivity）以保证沉浸式体验和内存隔离。

### 4.1 核心类图 (Class Diagram)

```
classDiagram
    class MainActivity {
        +switchFragment()
        +updateBottomTabUI()
    }
    class HomeFragment {
        -ViewPager2 viewPager
        -TabLayout tabLayout
    }
    class FeedFragment {
        -RecyclerView feedList
        -FeedViewModel viewModel
    }
    class PlayerActivity {
        -ViewPager2 verticalPager
        -PlayerViewModel viewModel
        +startPostponedEnterTransition()
    }
    class FeedAdapter {
        +bind()
        +onItemClick()
    }
    class PlayerAdapter {
        -ExoPlayer player
        +play()
        +release()
    }
    class FeedViewModel {
        +videoList LiveData
        +fetchFeed()
    }
    
    MainActivity --> HomeFragment : 包含
    HomeFragment --> FeedFragment : 包含 (ViewPager2)
    FeedFragment --> FeedViewModel : 观察数据
    FeedFragment ..> FeedAdapter : 使用
    FeedAdapter ..> PlayerActivity : 跳转 (共享元素转场)
    PlayerActivity --> PlayerViewModel : 观察数据
    PlayerActivity ..> PlayerAdapter : 使用
    PlayerAdapter ..> ExoPlayer : 封装
```

## 4.2 目录结构说明

```
com.zzh.tiktokdemo
├── feed                // 首页信息流模块
│   ├── FeedFragment.kt     // 瀑布流展示页
│   ├── HomeFragment.kt     // 顶部 Tab 容器
│   ├── CityFragment.kt     // 同城页
│   └── ...
├── network             // 网络层
│   └── RetrofitClient.kt
├── vedioclass          // 数据实体 (Model)
│   ├── VideoItem.kt
│   ├── Comment.kt
│   └── ...
├── AiChat* // AI 助手模块
├── Comment* // 评论区模块
├── Player* // 视频播放核心模块
├── DraggableFloatingView.kt // 自定义悬浮 View
└── MainActivity.kt     // App 入口
```

