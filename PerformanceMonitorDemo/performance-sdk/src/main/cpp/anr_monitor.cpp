#include <jni.h>
#include <string>
#include <android/log.h>
#include <signal.h>
#include <unistd.h>

#define TAG "NativeAnrMonitor"
// 定义简便的 log 宏
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// 全局变量，保存 Java 虚拟机引用和回调对象
JavaVM* gJavaVM = nullptr;
jobject gCallbackObject = nullptr;
struct sigaction old_sa; // 保存系统原来的信号处理函数

// C++ 调用 Java 的辅助函数
void notifyJavaAnr() {
    if (gJavaVM == nullptr || gCallbackObject == nullptr) {
        return;
    }

    JNIEnv* env;
    // 获取当前线程的 JNIEnv
    if (gJavaVM->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        if (gJavaVM->AttachCurrentThread(&env, nullptr) != JNI_OK) {
            LOGE("Failed to attach thread");
            return;
        }
    }

    // 找到 Java 类和方法
    jclass cls = env->GetObjectClass(gCallbackObject);
    // 对应 Java 层：fun onAnrHappened()
    jmethodID mid = env->GetMethodID(cls, "onAnrHappened", "()V");

    if (mid != nullptr) {
        env->CallVoidMethod(gCallbackObject, mid);
        LOGI("Called Java onAnrHappened successfully");
    }

    // 清理引用
    env->DeleteLocalRef(cls);
    // gJavaVM->DetachCurrentThread();
}

// 信号处理函数
void signalHandler(int sig, siginfo_t* info, void* context) {
    if (sig == SIGQUIT) {
        LOGE("SIGQUIT detected! ANR might be happening!");

        // 1. 通知 Java 层
        notifyJavaAnr();

        // 把信号重新发给系统原来的处理器
        if (old_sa.sa_handler != SIG_IGN && old_sa.sa_handler != SIG_DFL) {
            old_sa.sa_handler(sig); // 如果旧的是个函数，就调用它
        } else if (old_sa.sa_sigaction != nullptr) {
            old_sa.sa_sigaction(sig, info, context); // 如果旧的是 sigaction，就调用它
        }
    }
}

// JNI 初始化入口
extern "C" JNIEXPORT void JNICALL
Java_com_example_performance_1sdk_AnrMonitor_nativeInit(
        JNIEnv* env,
        jobject thiz) {

    // 1. 保存 JVM 引用
    env->GetJavaVM(&gJavaVM);
    gCallbackObject = env->NewGlobalRef(thiz);

    // 2. 注册信号监听
    struct sigaction sa;
    memset(&sa, 0, sizeof(sa));
    sa.sa_sigaction = signalHandler;
    sa.sa_flags = SA_ONSTACK | SA_SIGINFO;

    if (sigaction(SIGQUIT, &sa, &old_sa) != 0) {
        LOGE("Failed to register signal handler");
    } else {
        LOGI("ANR Monitor initialized successfully (SIGQUIT registered)");
    }


    sigset_t sigset;
    sigemptyset(&sigset);
    sigaddset(&sigset, SIGQUIT);
    // SIG_UNBLOCK = 1，解除屏蔽
    int r = pthread_sigmask(SIG_UNBLOCK, &sigset, nullptr);
    if (r == 0) {
        LOGI("SIGQUIT unblocked on init thread - Ready to catch ANR!");
    } else {
        LOGE("Failed to unblock SIGQUIT");
    }
}