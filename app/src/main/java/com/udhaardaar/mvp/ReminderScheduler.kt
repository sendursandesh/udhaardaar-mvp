package com.udhaardaar.mvp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ReminderScheduler {
    private const val ACTION = "com.udhaardaar.mvp.REPAYMENT_DUE"
    fun schedule(context: Context, creditId: Long, dueDate: String?, daysBefore: Int = 1) {
        if (creditId <= 0 || dueDate.isNullOrBlank()) return
        val parsed = try { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(dueDate) } catch (_: Exception) {
            try { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dueDate) } catch (_: Exception) { null }
        } ?: return
        val cal = Calendar.getInstance().apply { time = parsed; add(Calendar.DAY_OF_YEAR, -daysBefore); set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }
        if (cal.timeInMillis <= System.currentTimeMillis()) return
        val intent = Intent(context, ReminderReceiver::class.java).setAction(ACTION).putExtra("credit_id", creditId).putExtra("due_date", dueDate)
        val pi = PendingIntent.getBroadcast(context, creditId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
    }
}
