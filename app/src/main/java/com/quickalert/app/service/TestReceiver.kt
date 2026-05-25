package com.quickalert.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 测试用广播接收器 - 可以通过 adb 模拟短信匹配触发
 * 用法: adb shell am broadcast -a com.quickalert.app.TEST_ALERT -n com.quickalert.app/.service.TestReceiver --es sender "10086" --es text "【交警提醒】您的车辆有违章"
 */
class TestReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val sender = intent?.getStringExtra("sender") ?: "10086"
        val text = intent?.getStringExtra("text") ?: "【交警提醒】您的车辆有违章"
        Log.d(TAG, "=== 测试触发: sender=$sender, text=$text ===")
        AlarmForegroundService.start(context, sender, text, null, true)
        Log.d(TAG, "✅ 前台服务已启动")
    }
    companion object {
        private const val TAG = "TestReceiver"
    }
}
