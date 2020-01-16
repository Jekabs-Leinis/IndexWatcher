package com.leinis.indexwatcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager

class AutoRun : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val SP = PreferenceManager.getDefaultSharedPreferences(context)
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && SP.getBoolean("autorun", false)) {
            AlarmService().setAlarm(context)
        }
    }
}