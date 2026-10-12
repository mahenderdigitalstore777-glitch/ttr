package com.example.util

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashHandler private constructor(private val context: Context) : Thread.UncaughtExceptionHandler {
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val crashDir = File(context.filesDir, "crashes")
            if (!crashDir.exists()) crashDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val crashFile = File(crashDir, "crash_$timestamp.txt")
            val stackTrace = Log.getStackTraceString(throwable)
            crashFile.writeText("CRASH AT $timestamp:\nThread: ${thread.name}\n\n$stackTrace")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Delegate to system default handler
        defaultHandler?.uncaughtException(thread, throwable)
    }

    companion object {
        fun install(context: Context) {
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler(context))
        }
    }
}
