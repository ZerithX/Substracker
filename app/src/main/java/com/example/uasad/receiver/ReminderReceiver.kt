package com.example.uasad.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Logika notifikasi nanti akan diimplementasi di sini
        val subId = intent.getIntExtra("SUBSCRIPTION_ID", -1)
        Log.d("ReminderReceiver", "Alarm received for subscription ID: $subId")
    }
}
