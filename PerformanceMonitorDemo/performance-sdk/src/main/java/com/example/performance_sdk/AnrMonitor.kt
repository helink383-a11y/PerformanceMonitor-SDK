package com.example.performance_sdk

import android.util.Log

/**
 * AnrMonitor
 * 负责加载 Native 库并接收 Native 层回调
 */
internal class AnrMonitor {
    private val TAG = "AnrMonitor"
    var listener: PerformanceListener? = null

    companion object {
        init {
            try {
                System.loadLibrary("performance_monitor")
            } catch (e: UnsatisfiedLinkError) {
                Log.e("AnrMonitor", "Failed to load native library: ${e.message}")
            }
        }
    }

    fun start() {
        // 调用 Native 方法进行初始化
        nativeInit()
    }

    // 定义 Native 方法
    private external fun nativeInit()

    fun onAnrHappened() {
        Log.e(TAG, "ANR Detected from Native!")
        // 回调给上层
        listener?.onAnrHappened("ANR detected via SIGQUIT signal")
    }
}