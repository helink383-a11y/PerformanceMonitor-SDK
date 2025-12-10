package com.example.performance_sdk

/**
 * PerformanceListener
 * 用于将监控到的数据回调给 App
 */
interface PerformanceListener {
    /**
     * 当检测到卡顿（丢帧）时回调
     * @param skippedFrames 丢帧数量
     * @param costTimeMs 该帧耗时(毫秒)
     */
        fun onBlock(skippedFrames: Long, costTimeMs: Long)
        fun onAnrHappened(info: String)
}