package com.quickalert.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alert_rules")
data class AlertRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val senderPattern: String = "",      // 匹配发送方号码（正则），空则不匹配
    val keywordPattern: String = "",     // 匹配关键词（正则），空则不匹配
    val senderMatchType: Int = MATCH_CONTAINS,  // 号码匹配类型
    val keywordMatchType: Int = MATCH_CONTAINS,  // 关键词匹配类型
    val enabled: Boolean = true,
    val customRingtoneUri: String? = null,  // 自定义铃声 URI，null 使用默认
    val vibrate: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val MATCH_CONTAINS = 0   // 包含
        const val MATCH_EXACT = 1      // 精确匹配
        const val MATCH_STARTS_WITH = 2 // 以此开头
        const val MATCH_REGEX = 3      // 正则匹配
    }

    fun matchesSender(sender: String): Boolean {
        if (senderPattern.isBlank()) return true
        return when (senderMatchType) {
            MATCH_CONTAINS -> sender.contains(senderPattern, ignoreCase = true)
            MATCH_EXACT -> sender.equals(senderPattern, ignoreCase = true)
            MATCH_STARTS_WITH -> sender.startsWith(senderPattern, ignoreCase = true)
            MATCH_REGEX -> try {
                senderPattern.toRegex(RegexOption.IGNORE_CASE).containsMatchIn(sender)
            } catch (_: Exception) { false }
            else -> false
        }
    }

    fun matchesKeyword(text: String): Boolean {
        if (keywordPattern.isBlank()) return true
        return when (keywordMatchType) {
            MATCH_CONTAINS -> text.contains(keywordPattern, ignoreCase = true)
            MATCH_EXACT -> text.equals(keywordPattern, ignoreCase = true)
            MATCH_STARTS_WITH -> text.startsWith(keywordPattern, ignoreCase = true)
            MATCH_REGEX -> try {
                keywordPattern.toRegex(RegexOption.IGNORE_CASE).containsMatchIn(text)
            } catch (_: Exception) { false }
            else -> false
        }
    }
}
