package com.example.uasad.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.uasad.MainActivity
import com.example.uasad.R
import java.text.NumberFormat
import java.util.Locale

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val subId = intent.getIntExtra("SUBSCRIPTION_ID", -1)
        if (subId == -1) return
        
        val name = intent.getStringExtra("SUBSCRIPTION_NAME") ?: "Layanan"
        val price = intent.getDoubleExtra("SUBSCRIPTION_PRICE", 0.0)
        val date = intent.getStringExtra("SUBSCRIPTION_DATE") ?: "segera"

        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val priceFormatted = formatRupiah.format(price).replace("Rp", "Rp ")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "reminder_channel_id"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Subscription Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for subscription reminder notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("OPEN_DETAIL", subId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            subId,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Ganti dengan icon yang sesuai jika ada
            .setContentTitle("Reminder Subscription")
            .setContentText("$name akan jatuh tempo $date — $priceFormatted")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(subId, notification)
    }
}
