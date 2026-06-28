package com.dasong.commerce

import android.app.Application
import com.dasong.commerce.online.OnlineManager
import com.dasong.commerce.util.CrashHandler
import com.dasong.commerce.util.LogUtil
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DaSongApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // 初始化日志系统（必须在 CrashHandler 之前，否则 crash 无法写入日志文件）
        LogUtil.init(this)

        // 注册全局 Crash 捕获（crash 信息自动写入日志文件）
        CrashHandler.register()

        // 初始化 OnlineManager（恢复持久化的 playerId，支持进程重启后重连）
        OnlineManager.init(this)
    }
}
