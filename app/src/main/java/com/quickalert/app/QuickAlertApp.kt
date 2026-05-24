package com.quickalert.app

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.quickalert.app.data.AppDatabase

class QuickAlertApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val alertChannel = NotificationChannel(
            CHANNEL_ALERT,
            getString(R.string.alert_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.alert_channel_desc)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(alertChannel)
    }

    companion object {
        const val CHANNEL_ALERT = "sms_alert_channel"
    }
}
