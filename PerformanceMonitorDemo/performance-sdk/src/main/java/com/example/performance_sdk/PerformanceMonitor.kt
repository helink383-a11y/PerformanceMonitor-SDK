package com.example.performance_sdk

import android.content.Context
import android.util.Log

object PerformanceMonitor {
    private const val TAG = "PerformanceMonitor"
    private var isInitialized = false

    // 持有监控模块的实例
    private val smoothnessMonitor = SmoothnessMonitor()
    private val anrMonitor = AnrMonitor()

    // 外部传入的监听器
    private var performanceListener: PerformanceListener? = null

    data class Config(
        val monitorSmoothness: Boolean = true,
        val monitorAnr: Boolean = true
    )

    fun init(context: Context, config: Config = Config()) {
        if (isInitialized) {
            Log.w(TAG, "SDK is already initialized.")
            return
        }

        // 把 Listener 传递给内部模块
        smoothnessMonitor.listener = performanceListener
        anrMonitor.listener = performanceListener

        if (config.monitorSmoothness) {
            smoothnessMonitor.start()
        }

        if (config.monitorAnr) {
            startAnrMonitor() // 阶段三实现
        }

        isInitialized = true
        Log.i(TAG, "PerformanceMonitor Initialized")
    }


    fun setListener(listener: PerformanceListener) {
        this.performanceListener = listener
        // 如果已经初始化了，需要更新内部引用的 listener
        smoothnessMonitor.listener = listener
    }

    private fun startAnrMonitor() {
        Log.d(TAG, "ANR Monitor (Native) not implemented yet.")
        anrMonitor.start()
    }
}