package com.leinis.indexwatcher

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlin.random.Random


class Alarm : BroadcastReceiver() {

    private var CHANNEL_ID = "IndexMessages"

    override fun onReceive(context: Context, intent: Intent) {
        makeRequestToSteam(context)
    }

    private fun makeRequestToSteam(context: Context) {
        Log.e("Alert", "Making a request")
        val SP = PreferenceManager.getDefaultSharedPreferences(context)
        val queue = Volley.newRequestQueue(context)
        val url =
            SP.getString("target_link", context.resources.getString(R.string.target_link_default))

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                processResponse(context, response)
            },
            Response.ErrorListener {
                Toast.makeText(context, "Failed to load!", Toast.LENGTH_LONG).show()
            }
        )

        queue.add(stringRequest)
    }

    private fun processResponse(context: Context, response: String) {
        val SP = PreferenceManager.getDefaultSharedPreferences(context)
        val string = SP.getString(
            "search_string",
            context.resources.getString(R.string.search_string_default)
        ) ?: return
        val notifyCount = response.split(string).size - 1
        sendNotification(context, notifyCount)
    }

    private fun sendNotification(context: Context, stringCount: Int) {
        val SP = PreferenceManager.getDefaultSharedPreferences(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_index_notification)
        when {
            stringCount != SP.getString(
                "search_count",
                "10"
            )?.toInt() ?: return -> buildPriorityNotification(
                context,
                builder
            )
            SP.getBoolean("passive_messages", false) -> {
                builder.setContentTitle(context.resources.getString(R.string.nothing_happened))
                builder.priority = NotificationCompat.PRIORITY_DEFAULT
            }
            else -> return
        }

        val notification = builder.build()
        val nm = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        nm.notify(Random.nextInt(), notification)
    }

    private fun buildPriorityNotification(context: Context, builder: NotificationCompat.Builder) {
        builder.setContentTitle(context.resources.getString(R.string.something_happened))
        builder.setLights(Color.RED, 3000, 3000)
        builder.setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
        builder.setStyle(NotificationCompat.InboxStyle())
        val audio = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.nuke)
        builder.setSound(audio)
        builder.priority = NotificationCompat.PRIORITY_HIGH
    }

    fun triggerAlarm(context: Context): Boolean {
        sendNotification(context, 999)

        return true
    }

    fun setAlarm(context: Context) {
        val SP = PreferenceManager.getDefaultSharedPreferences(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = getAlarmIntent(context)

        val minutes = SP.getString("timer_interval", "10")?.toInt() ?: return
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            (1000 * 60 * minutes).toLong(),
            intent
        )
    }

    private fun getAlarmIntent(context: Context): PendingIntent {
        val i = Intent(context, Alarm::class.java)
        return PendingIntent.getBroadcast(context, 0, i, 0)
    }

    fun stopAlarm(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = getAlarmIntent(context)
        am.cancel(intent)
    }
}