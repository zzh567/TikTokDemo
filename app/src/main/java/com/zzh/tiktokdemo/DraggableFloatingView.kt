package com.zzh.tiktokdemo

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.abs

/**
 * 一个可以被拖动，且能响应点击事件的 ImageView
 */
class DraggableFloatingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private var lastX = 0f
    private var lastY = 0f
    private var isDragging = false
    private val touchSlop = 10 // 用于区分点击和拖动的阈值
    private var startClickTime: Long = 0

    init {

        setImageResource(R.drawable.ic_aichat)
        setColorFilter(0xFF00BFFF.toInt())

        // 设置高程，确保在最上层
        elevation = 20f
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val rawX = event.rawX
        val rawY = event.rawY

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = rawX
                lastY = rawY
                isDragging = false
                startClickTime = System.currentTimeMillis()
                // 返回 true 表示我们要消费这个序列的事件
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = rawX - lastX
                val dy = rawY - lastY

                // 如果移动距离超过阈值，则认为是拖动状态
                if (abs(dx) > touchSlop || abs(dy) > touchSlop) {
                    isDragging = true
                }

                // 核心：通过改变 translation 来实现移动
                // 这里可以加上边界判断防止拖出屏幕，为了简化代码暂省略
                this.translationX += dx
                this.translationY += dy

                lastX = rawX
                lastY = rawY
                return true
            }
            MotionEvent.ACTION_UP -> {
                val clickDuration = System.currentTimeMillis() - startClickTime
                // 如果不是拖动状态，且按下的时间很短，则认为是点击
                if (!isDragging && clickDuration < 200) {
                    performClick()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    // 必须重写 performClick 以便无障碍服务正常工作
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}