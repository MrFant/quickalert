package com.quickalert.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.quickalert.app.QuickAlertApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 直接监听短信广播 - 不依赖 NotificationListenerService
 * 这种方式在后台也能可靠触发
 */
class SmsReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        Log.d(TAG, "=== 收到短信广播 ===")

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) {
            Log.w(TAG, "短信内容为空")
            return
        }

        // 按 sender 号码分组，合并同一条短信的多个分片
        val smsMap = mutableMapOf<String, StringBuilder>()
        for (msg in messages) {
            val sender = msg.displayOriginatingAddress ?: msg.originatingAddress ?: ""
            val body = msg.messageBody ?: ""
            smsMap.getOrPut(sender) { StringBuilder() }.append(body)
        }

        for ((sender, body) in smsMap) {
            val fullText = body.toString()
            Log.d(TAG, "发送方: $sender, 内容: $fullText")

            // 在协程中查询数据库（避免阻塞主线程）
            scope.launch {
                try {
                    val app = context.applicationContext as? QuickAlertApp ?: return@launch
                    val dao = app.database.alertRuleDao()
                    val enabledRules = dao.getEnabledRules()
                    Log.d(TAG, "启用的规则数量: ${enabledRules.size}")

                    for (rule in enabledRules) {
                        if (rule.matchesSender(sender) && rule.matchesKeyword(fullText)) {
                            Log.d(TAG, "✅ 规则匹配: ${rule.name}, 触发提醒")
                            AlarmForegroundService.start(
                                context,
                                sender,
                                fullText,
                                rule.customRingtoneUri,
                                rule.vibrate
                            )
                            break
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "检查规则时出错", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "SmsReceiver"
    }
}
