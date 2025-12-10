# CMakeLists.txt
cmake_minimum_required(VERSION 3.22.1)

project("performance_sdk_native")

# 导入 Android 日志库，方便我们在 C++ 里打印 Logcat
find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )

add_library( # Sets the name of the library.
        performance_monitor

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        anr_monitor.cpp )

# 链接库
target_link_libraries( # Specifies the target library.
        performance_monitor

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib} )