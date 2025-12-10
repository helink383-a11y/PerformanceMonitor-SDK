# PerformanceMonitor SDK

一个轻量级、低耦合的 Android 性能监控 SDK。
专注于**UI流畅度监控**与**Native层 ANR 捕获**，助你快速定位应用性能瓶颈。

## ✨ 核心功能

* **流畅度监控 (Smoothness)**
    * 基于 `Choreographer` 机制
    * 实时计算掉帧 (Skipped Frames) 与 帧耗时
    * 支持自定义卡顿阈值
* **ANR 监控 (ANR Tracker)**
    * **硬核技术**：基于 Native (C++) 层捕获 `SIGQUIT` 信号
    * 在系统生成 traces.txt 之前抢先感知 ANR
    * 兼容 Android 8.0+ (API 26+)
    * 不影响系统原本的 ANR 处理流程

## 🛠 架构设计

* **模块化设计**：SDK 以独立 Module (`:performance-sdk`) 存在，不侵入业务代码。
* **分层架构**：
    * `Java/Kotlin 层`：负责配置初始化、回调分发。
    * `Native (C++) 层`：负责底层信号监听 (`pthread_sigmask`, `sigaction`)。
* **无侵入调用**：仅需在 Application 中初始化，通过 Listener 获取数据。

## 🚀 快速接入

### 1. 引入依赖
在主工程的 `build.gradle.kts` 中添加：
```kotlin
implementation(project(":performance-sdk"))
