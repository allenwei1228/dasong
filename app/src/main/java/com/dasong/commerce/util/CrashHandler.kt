package com.dasong.commerce.util

import android.os.Process
import java.io.PrintWriter
import java.io.StringWriter

/**
 * 全局 Crash 捕获器
 * 捕获未处理的异常，写入日志文件后再交给系统默认处理
 */
object CrashHandler : Thread.UncaughtExceptionHandler {

    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    fun register() {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        // 记录崩溃信息到日志文件
        LogUtil.e(
            "CrashHandler",
            "=== APP CRASH ===\n" +
                "Thread: ${thread.name}\n" +
                "Exception: ${throwableToString(throwable)}"
        )

        // 交给系统默认处理器（会弹出 ANR/Crash 对话框）
        defaultHandler?.uncaughtException(thread, throwable)

        // 如果没有默认处理器，手动结束进程
        Process.killProcess(Process.myPid())
    }

    private fun throwableToString(tr: Throwable): String {
        val sw = StringWriter()
        tr.printStackTrace(PrintWriter(sw))
        return sw.toString()
    }
}
