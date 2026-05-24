package com.quickalert.app.service

import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.quickalert.app.QuickAlertApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SmsNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // 常见短信 App 的包名列表
    private val smsPackages = setOf(
        "com.android.mms",
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging",
        "com.miui.sms",
        "com.android.messaging",
        "com.huawei.android.mms",
        "com.coloros.mms",
        "com.sonyericsson.conversations",
        "com.htc.sense.mms",
        "com.lge.mms",
        "com.skt.skmsg",
        "com.kt.showang",
        "com.android.mms.ui",
        "com.mediatek.mms",
        "org.smssecure",
        "com.klinker.android.evolve",
        "com.nextincloud.mmssms",
        "com.wunderground.android.weather"
    )

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "=== SmsNotificationListener 服务已创建 ===")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "=== 通知监听已连接 ===")
        // 启动常驻前台服务，防止进程被 ColorOS 冻结
        KeepAliveService.start(this)
        Log.d(TAG, "✅ KeepAlive 服务已启动")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "=== 通知监听已断开 ===")
        // 停止常驻服务
        KeepAliveService.stop(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) {
            Log.w(TAG, "onNotificationPosted: sbn is null")
            return
        }

        val packageName = sbn.packageName ?: return
        
        // 跳过自己发出的通知，避免循环触发
        if (packageName == this.packageName) return
        
        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        // 获取通知内容
        val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT)?.toString() ?: text

        // 构建完整的通知文本
        val fullText = if (bigText.isNotBlank()) bigText else text
        val sender = title

        Log.d(TAG, "=== 收到通知 ===")
        Log.d(TAG, "包名: $packageName")
        Log.d(TAG, "发送方: $sender")
        Log.d(TAG, "内容: $fullText")

        // 检查是否匹配任何规则
        scope.launch {
            try {
                val app = application as? QuickAlertApp ?: return@launch
                val dao = app.database.alertRuleDao()
                val enabledRules = dao.getEnabledRules()
                Log.d(TAG, "启用的规则数量: ${enabledRules.size}")
                
                for (rule in enabledRules) {
                    Log.d(TAG, "检查规则: ${rule.name}, 发送方模式: '${rule.senderPattern}', 关键词模式: '${rule.keywordPattern}'")
                    Log.d(TAG, "  发送方匹配结果: ${rule.matchesSender(sender)}")
                    Log.d(TAG, "  关键词匹配结果: ${rule.matchesKeyword(fullText)}")
                    
                    if (rule.matchesSender(sender) && rule.matchesKeyword(fullText)) {
                        Log.d(TAG, "✅ 规则匹配: ${rule.name}, 触发提醒")
                        triggerAlert(sender, fullText, rule.customRingtoneUri, rule.vibrate)
                        break  // 只触发第一个匹配的规则
                    }
                }
                
                if (enabledRules.isEmpty()) {
                    Log.w(TAG, "⚠️ 没有启用的规则，请先在App中添加规则")
                }
            } catch (e: Exception) {
                Log.e(TAG, "检查规则时出错", e)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // 通知被移除时不做处理
    }

    private fun triggerAlert(
        sender: String,
        text: String,
        customRingtoneUri: String?,
        vibrate: Boolean
    ) {
        Log.d(TAG, "=== 触发提醒: sender=$sender ===")
        // 直接启动前台服务来播放报警声+振动+全屏提醒
        AlarmForegroundService.start(this, sender, text, customRingtoneUri, vibrate)
        Log.d(TAG, "✅ 前台服务已启动")
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    companion object {
        private const val TAG = "SmsNotificationListener"
    }
}
