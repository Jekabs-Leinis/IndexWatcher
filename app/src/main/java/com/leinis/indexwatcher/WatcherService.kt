package com.leinis.indexwatcher

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log


class WatcherService : Service() {
    private var alarm = Alarm()
    private var TAG = "WarcherService"

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")

        alarm.setAlarm(this)
        return START_STICKY
    }

    override fun onStart(intent: Intent, startId: Int) {
        Log.d(TAG, "Service started")
        alarm.setAlarm(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d(TAG, "Service stopped")
        alarm.stopAlarm(this)
    }
}