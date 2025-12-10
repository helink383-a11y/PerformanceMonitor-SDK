package com.example.performancemonitor

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.performance_sdk.PerformanceListener
import com.example.performance_sdk.PerformanceMonitor

class MainActivity : AppCompatActivity() {

    private lateinit var logTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logTextView = findViewById(R.id.tv_log)
        val btnLag = findViewById<Button>(R.id.btn_test_lag)

        // 1. 设置 SDK 监听器 (在 init 之前)
        PerformanceMonitor.setListener(object : PerformanceListener {
            override fun onBlock(skippedFrames: Long, costTimeMs: Long) {
                runOnUiThread {
                    val info = "检测到卡顿: 耗时 ${costTimeMs}ms, 丢帧 $skippedFrames"
                    appendLog(info)
                    Toast.makeText(this@MainActivity, "卡顿! $info", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onAnrHappened(info: String) {
                runOnUiThread {
                    val logMsg = "【严重】检测到 ANR (Native信号): $info"
                    appendLog(logMsg)
                    Toast.makeText(this@MainActivity, "发生 ANR! 请查看日志", Toast.LENGTH_LONG).show()


                    android.util.Log.e("MainActivity", logMsg)
                }
            }
        })

        // 2. 初始化 SDK
        PerformanceMonitor.init(this)

        // 3. 点击按钮，故意让主线程睡 800毫秒，模拟重度卡顿
        btnLag.setOnClickListener {
            appendLog("正在模拟卡顿(休眠800ms)...")
            try {
                Thread.sleep(800)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        val btnAnrSignal = findViewById<Button>(R.id.btn_test_anr_signal)

        btnAnrSignal.setOnClickListener {
            appendLog("正在向自己发送 SIGQUIT 信号...")
            // android.os.Process.sendSignal 不需要 root 权限，只要是发给自己就行
            // 3 代表 SIGQUIT
            android.os.Process.sendSignal(android.os.Process.myPid(), 3)
        }
    }

    private fun appendLog(text: String) {
        val currentText = logTextView.text.toString()
        logTextView.text = "$text\n$currentText"
    }
}