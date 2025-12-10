package com.example.performance_sdk

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Choreographer
import java.util.concurrent.TimeUnit

/**
 * SmoothnessMonitor
 * 基于 Choreographer 的流畅度监控
 */
internal class SmoothnessMonitor : Choreographer.FrameCallback {

    private val TAG = "SmoothnessMonitor"
    private var lastFrameTimeNanos: Long = 0
    private var isMonitoring = false

    // 监听器引用
    var listener: PerformanceListener? = null

    // 标准帧率 60Hz -> 16.6ms/帧
    private val frameIntervalNanos = 16_666_666L
    private val blockThresholdMs = 80L

    // 确保在主线程操作 Choreographer
    private val mainHandler = Handler(Looper.getMainLooper())

    fun start() {
        if (isMonitoring) return
        isMonitoring = true
        // 必须在主线程注册
        mainHandler.post {
            Choreographer.getInstance().postFrameCallback(this)
        }
        Log.d(TAG, "SmoothnessMonitor started")
    }

    fun stop() {
        isMonitoring = false
        mainHandler.post {
            Choreographer.getInstance().removeFrameCallback(this)
        }
        Log.d(TAG, "SmoothnessMonitor stopped")
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!isMonitoring) return

        if (lastFrameTimeNanos != 0L) {
            // 计算两帧之间的时间差 (纳秒 -> 毫秒)
            val diffNanos = frameTimeNanos - lastFrameTimeNanos
            val diffMs = TimeUnit.NANOSECONDS.toMillis(diffNanos)

            // 如果这一帧处理时间超过了标准间隔，说明发生了丢帧
            if (diffNanos > frameIntervalNanos) {
                // 计算大概丢了多少帧
                val skippedFrames = diffNanos / frameIntervalNanos

                if (diffMs > blockThresholdMs) {
                    Log.w(TAG, "检测到卡顿! 耗时: ${diffMs}ms, 丢帧: $skippedFrames")
                    // 回调给 App
                    listener?.onBlock(skippedFrames, diffMs)
                }
            }
        }

        lastFrameTimeNanos = frameTimeNanos

        // 注册下一帧的回调，实现循环监控
        Choreographer.getInstance().postFrameCallback(this)
    }
}