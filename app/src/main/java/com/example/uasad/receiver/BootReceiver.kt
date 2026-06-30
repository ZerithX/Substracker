package com.example.uasad.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.uasad.data.DatabaseBuilder
import com.example.uasad.settings.SettingsFragment
import com.example.uasad.utils.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val database = DatabaseBuilder.getInstance(context)
            val subscriptionDao = database.subscriptionDao()

            CoroutineScope(Dispatchers.IO).launch {
                val subscriptions = subscriptionDao.getAll().firstOrNull()
                subscriptions?.forEach { subscription ->
                    if (subscription.reminderEnabled) {
                        val daysBefore = SettingsFragment.getReminderDaysBefore(context)
                        AlarmScheduler.scheduleReminder(context, subscription, daysBefore)
                    }
                }
            }
        }
    }
}
