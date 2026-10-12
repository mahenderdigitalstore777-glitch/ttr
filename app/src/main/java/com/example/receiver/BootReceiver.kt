package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.MahiCricDatabase
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver {
    // Zero-arg constructor required by Android system
    constructor() : super()

    // Test constructor for dependency injection during unit tests
    internal var databaseProvider: (Context) -> MahiCricDatabase = { context ->
        MahiCricDatabase.getInstance(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = databaseProvider(context)
                    val activeMatch = db.matchDao().getActiveMatchSync()
                    if (activeMatch != null) {
                        NotificationHelper.createNotificationChannel(context)
                        NotificationHelper.showMatchInProgressNotification(
                            context,
                            "${activeMatch.teamAShortName} vs ${activeMatch.teamBShortName}",
                            "Your match was in progress. Tap to resume scoring!"
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
