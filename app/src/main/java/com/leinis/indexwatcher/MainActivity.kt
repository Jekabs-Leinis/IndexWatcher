package com.leinis.indexwatcher

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (isAlarmSet()) {
            val text = findViewById<TextView>(R.id.status_text)
            text.setText(R.string.status_on)
        }
    }

    fun toggleService(view: View) {
        val text = findViewById<TextView>(R.id.status_text)
        if (!isAlarmSet()) {
            AlarmService().setAlarm(this)
            text.setText(R.string.status_on)
        } else {
            AlarmService().stopAlarm(this)
            text.setText(R.string.status_off)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_settings -> showSettings()
        R.id.action_test_alarm -> AlarmService().triggerAlarm(this)
        else -> super.onOptionsItemSelected(item)
    }

    private fun showSettings(): Boolean {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        return true
    }

    private fun isAlarmSet(): Boolean {
        return PendingIntent.getBroadcast(
            this,
            0,
            Intent(this, AlarmService::class.java),
            PendingIntent.FLAG_NO_CREATE
        ) != null
    }
}
