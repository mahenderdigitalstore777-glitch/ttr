package com.example.util

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object SnapshotManager {
    private const val SNAPSHOTS_DIR = "safety_snapshots"
    private const val MAX_ROLLING_SNAPSHOTS = 5

    fun saveRollingSnapshot(
        context: Context,
        matchId: String,
        overNumber: Int,
        snapshotJson: String
    ) {
        try {
            val dir = File(context.filesDir, SNAPSHOTS_DIR)
            if (!dir.exists()) dir.mkdirs()

            val matchDir = File(dir, matchId)
            if (!matchDir.exists()) matchDir.mkdirs()

            val timestamp = System.currentTimeMillis()
            val fileName = "snap_over_${overNumber}_$timestamp.json"
            val file = File(matchDir, fileName)
            file.writeText(snapshotJson)

            // Keep only the newest MAX_ROLLING_SNAPSHOTS files
            val files = matchDir.listFiles()?.sortedByDescending { it.lastModified() } ?: return
            if (files.size > MAX_ROLLING_SNAPSHOTS) {
                files.drop(MAX_ROLLING_SNAPSHOTS).forEach { it.delete() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLatestSnapshot(context: Context, matchId: String): String? {
        val dir = File(context.filesDir, SNAPSHOTS_DIR)
        val matchDir = File(dir, matchId)
        if (!matchDir.exists()) return null
        val files = matchDir.listFiles()?.sortedByDescending { it.lastModified() } ?: return null
        return files.firstOrNull()?.readText()
    }

    fun listAvailableSnapshots(context: Context, matchId: String): List<File> {
        val dir = File(context.filesDir, SNAPSHOTS_DIR)
        val matchDir = File(dir, matchId)
        if (!matchDir.exists()) return emptyList()
        return matchDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
