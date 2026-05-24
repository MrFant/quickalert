package com.quickalert.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.quickalert.app.QuickAlertApp
import com.quickalert.app.R
import com.quickalert.app.ui.AlertFullScreenActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmForegroundService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "=== AlarmForegroundService 创建 ===")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sender = intent?.getStringExtra(EXTRA_SENDER) ?: "未知"
        val text = intent?.getStringExtra(EXTRA_TEXT) ?: ""
        val ringtoneUri = intent?.getStringExtra(EXTRA_RINGTONE_URI)
        val vibrate = intent?.getBooleanExtra(EXTRA_VIBRATE, true) ?: true

        Log.d(TAG, "=== 启动前台服务: sender=$sender ===")

        // 创建通知渠道
        val nm = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "短信提醒",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "短信强提醒服务"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        nm.createNotificationChannel(channel)

        // 创建停止 Intent
        val stopIntent = Intent(this, AlarmStopReceiver::class.java)
        val stopPendingIntent = PendingIntent.getBroadcast(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 启动前台服务
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("🔔 快提醒")
            .setContentText("来自 $sender: $text")
            .setStyle(Notification.BigTextStyle().bigText("来自 $sender: $text"))
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher, "关闭提醒", stopPendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        // 播放铃声
        startAlarm(ringtoneUri)

        // 振动
        if (vibrate) {
            startVibrate()
        }

        // 尝试启动全屏 Activity
        tryStartFullScreenActivity(sender, text, ringtoneUri, vibrate)

        // 30秒后自动停止（防止无限循环）
        scope.launch {
            delay(30_000)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun startAlarm(ringtoneUri: String?) {
        try {
            val uri = if (ringtoneUri != null) {
                Uri.parse(ringtoneUri)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(this@AlarmForegroundService, uri)
                isLooping = true
                prepare()
                start()
            }
            Log.d(TAG, "✅ 铃声播放中")
        } catch (e: Exception) {
            Log.e(TAG, "播放铃声失败，尝试默认铃声", e)
            try {
                val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(this@AlarmForegroundService, defaultUri)
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (_: Exception) { }
        }
    }

    private fun startVibrate() {
        val pattern = longArrayOf(0, 500, 200, 500, 200, 500, 200, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        Log.d(TAG, "✅ 振动中")
    }

    private fun tryStartFullScreenActivity(
        sender: String,
        text: String,
        ringtoneUri: String?,
        vibrate: Boolean
    ) {
        try {
            val intent = Intent(this, AlertFullScreenActivity::class.java).apply {
                putExtra(AlertFullScreenActivity.EXTRA_SENDER, sender)
                putExtra(AlertFullScreenActivity.EXTRA_TEXT, text)
                putExtra(AlertFullScreenActivity.EXTRA_RINGTONE_URI, ringtoneUri)
                putExtra(AlertFullScreenActivity.EXTRA_VIBRATE, vibrate)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            }
            startActivity(intent)
            Log.d(TAG, "✅ 全屏 Activity 已启动")
        } catch (e: Exception) {
            Log.e(TAG, "启动全屏 Activity 失败", e)
        }
    }

    private fun stopAlarm() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) { }
        mediaPlayer = null

        try {
            vibrator?.cancel()
        } catch (_: Exception) { }
        vibrator = null
    }

    override fun onDestroy() {
        stopAlarm()
        scope.cancel()
        // 取消前台通知
        val nm = getSystemService(NotificationManager::class.java)
        nm.cancel(NOTIFICATION_ID)
        Log.d(TAG, "=== AlarmForegroundService 已停止 ===")
        super.onDestroy()
    }

    companion object {
        private const val TAG = "AlarmForegroundService"
        private const val CHANNEL_ID = "alarm_foreground_channel"
        private const val NOTIFICATION_ID = 8888
        const val EXTRA_SENDER = "extra_sender"
        const val EXTRA_TEXT = "extra_text"
        const val EXTRA_RINGTONE_URI = "extra_ringtone_uri"
        const val EXTRA_VIBRATE = "extra_vibrate"

        fun start(
            context: Context,
            sender: String,
            text: String,
            ringtoneUri: String?,
            vibrate: Boolean
        ) {
            val intent = Intent(context, AlarmForegroundService::class.java).apply {
                putExtra(EXTRA_SENDER, sender)
                putExtra(EXTRA_TEXT, text)
                putExtra(EXTRA_RINGTONE_URI, ringtoneUri)
                putExtra(EXTRA_VIBRATE, vibrate)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AlarmForegroundService::class.java))
        }
    }
}
