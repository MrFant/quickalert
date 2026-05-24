package com.quickalert.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d(TAG, "收到停止信号")
        AlarmForegroundService.stop(context)
    }

    companion object {
        private const val TAG = "AlarmStopReceiver"
    }
}
