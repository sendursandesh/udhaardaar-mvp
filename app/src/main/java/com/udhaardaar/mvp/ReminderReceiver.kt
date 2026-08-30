package com.udhaardaar.mvp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val channelId = "udhaardaar_due"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(NotificationChannel(channelId, "Udhaardaar repayment reminders", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val due = intent.getStringExtra("due_date") ?: "today"
        val kind = intent.getStringExtra("credit_kind") ?: "CREDIT"
        val label = if (kind == "FORMAL") "formal loan" else "informal credit"
        manager.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Udhaardaar repayment due")
            .setContentText("A $label repayment is scheduled for $due. Open Udhaardaar to record the payment.")
            .setAutoCancel(true).build())
    }
}
