package com.leinis.indexwatcher

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlin.random.Random


class Alarm : BroadcastReceiver() {

    private var CHANNEL_ID = "IndexMessages"
    private var TAG = "Alert"
    private lateinit var SP: SharedPreferences

    override fun onReceive(context: Context, intent: Intent) {
        SP = context.getSharedPreferences(
            context.packageName + "_preferences",
            Context.MODE_MULTI_PROCESS
        )
        makeRequestToSteam(context)
    }

    private fun makeRequestToSteam(context: Context) {
        Log.d(TAG, "Making a request")
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
        val string = SP.getString(
            "search_string",
            context.resources.getString(R.string.search_string_default)
        ) ?: return
        val notifyCount = response.split(string).size - 1
        sendNotification(context, notifyCount)
    }

    private fun sendNotification(context: Context, stringCount: Int) {

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_index_notification)
        Log.d(TAG, "Search count: " + SP.getString("search_count", "69"))
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
        SP = context.getSharedPreferences(
            context.packageName + "_preferences",
            Context.MODE_MULTI_PROCESS
        )
        sendNotification(context, 999)

        return true
    }

    fun setAlarm(context: Context) {
        val SP = context.getSharedPreferences(
            context.packageName + "_preferences",
            Context.MODE_MULTI_PROCESS
        )
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