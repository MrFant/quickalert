package com.quickalert.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * 常驻前台服务 - 防止进程被 ColorOS 冻结
 * NotificationListenerService 需要进程保持活跃才能接收回调
 */
class KeepAliveService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "=== KeepAliveService 创建 ===")
        startForegroundNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "=== KeepAliveService 启动 ===")
        return START_STICKY  // 被杀后自动重启
    }

    private fun startForegroundNotification() {
        val nm = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "快提醒后台服务",
            NotificationManager.IMPORTANCE_LOW  // 低优先级，不打扰用户
        ).apply {
            description = "保持短信监听服务运行"
            setShowBadge(false)
        }
        nm.createNotificationChannel(channel)

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(com.quickalert.app.R.drawable.ic_launcher)
            .setContentTitle("快提醒正在运行")
            .setContentText("正在监听短信通知...")
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        Log.d(TAG, "✅ 前台通知已显示")
    }

    override fun onDestroy() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.cancel(NOTIFICATION_ID)
        Log.d(TAG, "=== KeepAliveService 已停止 ===")
        super.onDestroy()
    }

    companion object {
        private const val TAG = "KeepAliveService"
        private const val CHANNEL_ID = "keep_alive_channel"
        private const val NOTIFICATION_ID = 7777

        fun start(context: Context) {
            val intent = Intent(context, KeepAliveService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, KeepAliveService::class.java))
        }
    }
}
