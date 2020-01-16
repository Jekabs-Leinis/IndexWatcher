package com.leinis.indexwatcher

import android.app.Service
import android.content.Intent
import android.os.IBinder

class WatcherService : Service() {
    private var alarm = Alarm()


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        alarm.setAlarm(this)
        return START_STICKY
    }

    override fun onStart(intent: Intent, startId: Int) {
        alarm.setAlarm(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        alarm.stopAlarm(this)
    }
}