package com.example.uasad.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.uasad.data.Subscription
import com.example.uasad.receiver.ReminderReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object AlarmScheduler {

    fun scheduleReminder(context: Context, subscription: Subscription, reminderDaysBefore: Int) {
        if (!subscription.reminderEnabled) {
            cancelReminder(context, subscription.id)
            return
        }

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val billingDate = sdf.parse(subscription.nextBilling) ?: return

            val calendar = Calendar.getInstance().apply { // Testing Only
                add(Calendar.SECOND, 1)
            }
            /* val calendar = Calendar.getInstance().apply {
                time = billingDate
                add(Calendar.DAY_OF_YEAR, -reminderDaysBefore)
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // Jika waktu alarm sudah terlewat, jangan set alarm (opsional bisa di-adjust)
            if (calendar.timeInMillis < System.currentTimeMillis()) {
                // Bisa juga di set untuk bulan depan, tapi biasanya handled saat insert/update
                return
            } */

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("SUBSCRIPTION_ID", subscription.id)
                putExtra("SUBSCRIPTION_NAME", subscription.name)
                putExtra("SUBSCRIPTION_PRICE", subscription.price)
                putExtra("SUBSCRIPTION_DATE", subscription.nextBilling)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                subscription.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Menggunakan setExactAndAllowWhileIdle karena kita ingin presisi jam 09:00
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelReminder(context: Context, subscriptionId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            subscriptionId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }
}
