package com.udhaardaar.mvp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Locale

object ReminderScheduler {
    private const val ACTION_DUE = "com.udhaardaar.mvp.ACTION_DUE_REMINDER"

    fun schedule(context: Context, creditId: Long, dueDate: String, kind: String) {
        val millis = parseDate(dueDate) ?: return
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_DUE
            putExtra("credit_id", creditId)
            putExtra("due_date", dueDate)
            putExtra("credit_kind", kind)
        }
        val requestCode = (creditId * 31 + kind.hashCode()).toInt()
        val pi = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
    }

    fun scheduleInformal(context: Context, creditId: Long, dueDate: String) = schedule(context, creditId, dueDate, "INFORMAL")
    fun scheduleFormal(context: Context, loanId: Long, dueDate: String) = schedule(context, loanId, dueDate, "FORMAL")

    private fun parseDate(value: String): Long? = try {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(value)?.time
    } catch (_: Exception) { null }
}
