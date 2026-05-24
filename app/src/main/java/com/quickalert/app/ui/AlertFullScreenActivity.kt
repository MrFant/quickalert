package com.quickalert.app.ui

import android.animation.ObjectAnimator
import android.app.KeyguardManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.TextView
import android.app.Activity

class AlertFullScreenActivity : Activity() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置全屏显示 + 锁屏显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            km.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        // 设置全屏沉浸式
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )

        val sender = intent.getStringExtra(EXTRA_SENDER) ?: "未知发送者"
        val text = intent.getStringExtra(EXTRA_TEXT) ?: ""
        val ringtoneUri = intent.getStringExtra(EXTRA_RINGTONE_URI)
        val vibrate = intent.getBooleanExtra(EXTRA_VIBRATE, true)

        // 使用代码创建简单 UI，不依赖 Compose（因为这是 Activity 而非 ComponentActivity）
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(0xFF1A1A2E.toInt())
            setPadding(48, 48, 48, 48)
        }

        val iconText = TextView(this)
        iconText.text = "🔔"
        iconText.textSize = 72f
        iconText.gravity = android.view.Gravity.CENTER

        val titleText = TextView(this)
        titleText.text = "短信提醒"
        titleText.textSize = 24f
        titleText.setTextColor(0xFFFF6B6B.toInt())
        titleText.gravity = android.view.Gravity.CENTER
        titleText.setPadding(0, 32, 0, 16)

        val senderText = TextView(this)
        senderText.text = "来自: $sender"
        senderText.textSize = 20f
        senderText.setTextColor(0xFFFFFFFF.toInt())
        senderText.gravity = android.view.Gravity.CENTER
        senderText.setPadding(0, 16, 0, 8)

        val contentText = TextView(this)
        contentText.text = text
        contentText.textSize = 16f
        contentText.setTextColor(0xFFE0E0E0.toInt())
        contentText.gravity = android.view.Gravity.CENTER
        contentText.setPadding(32, 16, 32, 32)
        contentText.maxLines = 8

        val dismissButton = Button(this)
        dismissButton.text = "关闭提醒"
        dismissButton.textSize = 18f
        dismissButton.setOnClickListener { dismissAlert() }
        dismissButton.layoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 64
        }

        layout.addView(iconText)
        layout.addView(titleText)
        layout.addView(senderText)
        layout.addView(contentText)
        layout.addView(dismissButton)

        setContentView(layout)

        // 开始播放铃声和振动
        startAlert(ringtoneUri, vibrate)
    }

    private fun startAlert(ringtoneUri: String?, vibrate: Boolean) {
        // 播放铃声（循环）
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
                setDataSource(this@AlertFullScreenActivity, uri)
                isLooping = true
                prepare()
                start()
            }
            isPlaying = true
        } catch (e: Exception) {
            e.printStackTrace()
            // 尝试使用默认铃声
            try {
                val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(this@AlertFullScreenActivity, defaultUri)
                    isLooping = true
                    prepare()
                    start()
                }
                isPlaying = true
            } catch (_: Exception) { }
        }

        // 振动（循环模式）
        if (vibrate) {
            val pattern = longArrayOf(0, 500, 200, 500, 200, 500, 200, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibrator = vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        }
    }

    private fun dismissAlert() {
        stopAlert()
        finish()
    }

    private fun stopAlert() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
        } catch (_: Exception) { }
        mediaPlayer = null

        try {
            vibrator?.cancel()
        } catch (_: Exception) { }
        vibrator = null
        isPlaying = false
    }

    override fun onDestroy() {
        stopAlert()
        super.onDestroy()
    }

    override fun onBackPressed() {
        dismissAlert()
    }

    companion object {
        const val EXTRA_SENDER = "extra_sender"
        const val EXTRA_TEXT = "extra_text"
        const val EXTRA_RINGTONE_URI = "extra_ringtone_uri"
        const val EXTRA_VIBRATE = "extra_vibrate"
    }
}
