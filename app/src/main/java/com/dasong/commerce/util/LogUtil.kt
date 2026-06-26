package com.dasong.commerce.util

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 日志工具类
 * - 同时输出到 logcat 和文件
 * - 支持手动导出/分享日志文件
 *
 * 使用方式：LogUtil.d("TAG", "message")  替代 android.util.Log.d()
 *
 * 日志文件路径：/sdcard/Android/data/com.dasong.commerce/cache/logs/app_yyyy-MM-dd.log
 * 导出方式：
 *   1. adb pull /sdcard/Android/data/com.dasong.commerce/cache/logs/ ./
 *   2. 调用 LogUtil.shareLogFile(context) 弹出分享面板
 */
object LogUtil {

    private const val LOG_DIR = "logs"
    private const val LOG_FILE_PREFIX = "app"
    private const val MAX_LOG_FILE_SIZE = 5 * 1024 * 1024L // 5MB 单文件上限
    private const val MAX_LOG_FILES = 5 // 最多保留 5 个日志文件

    private var logFile: File? = null
    private var logDir: File? = null
    private var isInitialized = false

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val fileDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    @Synchronized
    fun init(context: Context) {
        if (isInitialized) return

        try {
            // 优先使用 externalCacheDir，可直接 adb pull 无需 run-as
            // 回退到 filesDir（当外部存储不可用时）
            val baseDir = context.externalCacheDir ?: context.filesDir
            logDir = File(baseDir, LOG_DIR)
            if (logDir!!.exists().not()) {
                logDir!!.mkdirs()
            }

            // 清理旧日志文件（保留最近的 MAX_LOG_FILES 个）
            val logFiles = logDir!!.listFiles { file -> file.name.startsWith(LOG_FILE_PREFIX) }
            logFiles?.sortedByDescending { it.lastModified() }?.drop(MAX_LOG_FILES)?.forEach {
                it.delete()
            }

            // 创建当天的日志文件
            val today = fileDateFormat.format(Date())
            val file = File(logDir, "${LOG_FILE_PREFIX}_$today.log")

            // 如果文件太大，归档后新建
            if (file.exists() && file.length() > MAX_LOG_FILE_SIZE) {
                val archived = File(logDir, "${LOG_FILE_PREFIX}_${today}_${System.currentTimeMillis()}.log")
                file.renameTo(archived)
            }

            logFile = file
            isInitialized = true

            // 删除旧的 overflow 归档文件
            logDir!!.listFiles { f ->
                f.name.matches(Regex("${LOG_FILE_PREFIX}_\\d{4}-\\d{2}-\\d{2}_\\d+\\.log"))
            }?.sortedByDescending { it.lastModified() }?.drop(MAX_LOG_FILES)?.forEach {
                it.delete()
            }

            i("LogUtil", "=== 日志系统初始化，文件路径: ${file.absolutePath} ===")
        } catch (e: Exception) {
            Log.e("LogUtil", "日志系统初始化失败", e)
        }
    }

    /**
     * 获取日志文件路径（用于 adb pull 或导出）
     */
    fun getLogFilePath(): String? {
        return logFile?.absolutePath
    }

    /**
     * 获取日志目录下所有日志文件
     */
    fun getLogFiles(): List<File> {
        val dir = logDir ?: return emptyList()
        return dir.listFiles { file -> file.name.startsWith(LOG_FILE_PREFIX) }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    /**
     * 通过系统分享面板导出日志文件
     */
    fun shareLogFile(context: Context) {
        try {
            val files = getLogFiles()
            if (files.isEmpty()) {
                Log.w("LogUtil", "没有日志文件可分享")
                return
            }

            // 分享最新的日志文件
            val latestFile = files.first()

            // 复制到 cache 目录用于分享（避免 FileProvider 路径问题）
            val shareFile = File(context.cacheDir, latestFile.name)
            latestFile.copyTo(shareFile, overwrite = true)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.logfileprovider",
                shareFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "大宋百商图 - 日志")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "导出日志"))
        } catch (e: Exception) {
            Log.e("LogUtil", "分享日志文件失败", e)
        }
    }

    // ========== 日志输出方法 ==========

    fun v(tag: String, msg: String) {
        Log.v(tag, msg)
        writeToFile("V", tag, msg)
    }

    fun d(tag: String, msg: String) {
        Log.d(tag, msg)
        writeToFile("D", tag, msg)
    }

    fun i(tag: String, msg: String) {
        Log.i(tag, msg)
        writeToFile("I", tag, msg)
    }

    fun w(tag: String, msg: String) {
        Log.w(tag, msg)
        writeToFile("W", tag, msg)
    }

    fun w(tag: String, msg: String, tr: Throwable) {
        Log.w(tag, msg, tr)
        writeToFile("W", tag, "$msg\n${throwableToString(tr)}")
    }

    fun e(tag: String, msg: String) {
        Log.e(tag, msg)
        writeToFile("E", tag, msg)
    }

    fun e(tag: String, msg: String, tr: Throwable) {
        Log.e(tag, msg, tr)
        writeToFile("E", tag, "$msg\n${throwableToString(tr)}")
    }

    // ========== 内部方法 ==========

    @Synchronized
    private fun writeToFile(level: String, tag: String, msg: String) {
        val file = logFile ?: return
        try {
            val timestamp = dateFormat.format(Date())
            val line = "$timestamp $level/$tag: $msg\n"
            file.appendText(line)
        } catch (e: Exception) {
            // 写文件失败不抛异常，避免影响主流程
            Log.e("LogUtil", "写入日志文件失败", e)
        }
    }

    private fun throwableToString(tr: Throwable): String {
        val sw = StringWriter()
        tr.printStackTrace(PrintWriter(sw))
        return sw.toString()
    }
}
