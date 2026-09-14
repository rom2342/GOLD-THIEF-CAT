package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.security.PrivacyBlockManager

class PrivacyGuardService : Service() {

    private lateinit var blockManager: PrivacyBlockManager

    companion object {
        const val CHANNEL_ID = "privacy_blocker_channel"
        const val NOTIFICATION_ID = 101
        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
        const val ACTION_TOGGLE_MIC = "com.example.service.ACTION_TOGGLE_MIC"
        const val ACTION_TOGGLE_CAMERA = "com.example.service.ACTION_TOGGLE_CAMERA"

        fun startService(context: Context) {
            val intent = Intent(context, PrivacyGuardService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, PrivacyGuardService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        blockManager = PrivacyBlockManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(true)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_MIC -> {
                val current = blockManager.isMicBlocked()
                blockManager.setMicBlocked(!current)
            }
            ACTION_TOGGLE_CAMERA -> {
                val current = blockManager.isCameraBlocked()
                blockManager.setCameraBlocked(!current)
            }
        }

        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isCamBlocked = blockManager.isCameraBlocked()
        val isMicBlocked = blockManager.isMicBlocked()

        val statusText = buildString {
            append("מצלמה: ")
            append(if (isCamBlocked) "חסומה 🔒" else "פתוחה ⚠️")
            append(" | מיקרופון: ")
            append(if (isMicBlocked) "מושתק 🔒" else "פעיל ⚠️")
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("מגן פרטיות פעיל 🛡️")
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_app_icon_fg)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
