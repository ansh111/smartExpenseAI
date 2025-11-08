package com.anshul.smartmediaai.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.anshul.smartmediaai.MainActivity
import com.anshul.smartmediaai.R

class NotificationHelper(private  val context: Context) {
    companion object {
        const val CHANNEL_ID_EXPENSES = "expense_alerts_channel"
        const val CHANNEL_NAME_EXPENSES = "Expense Alerts"
        const val NOTIFICATION_ID_NEW_EXPENSE = 1001
    }

    private fun createNotificationChannels() {
        val  channel = NotificationChannel(
            CHANNEL_ID_EXPENSES,
            CHANNEL_NAME_EXPENSES,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts for new expenses"
            enableLights(true)
            lightColor  = Color.BLUE
            enableVibration(true)

        }
        val  manager : NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

    }

    fun showNewNotification(title:String,message: String, expenseId: String) {
        val intent  = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            expenseId.let {
                putExtra("NAVIGATE_TO_EXPENSE_DETAILS", it)
            }
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_EXPENSES)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(NotificationManagerCompat.from(context).areNotificationsEnabled()){
                try {
                    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_NEW_EXPENSE, builder.build())
                }catch (e: SecurityException){
                    e.printStackTrace()
                }
            } else {
                Log.w("NotificationHelper", "Notification permission not granted")
            }
        } else {
            try {
                NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_NEW_EXPENSE, builder.build())
            } catch (e: SecurityException) {
                android.util.Log.e("NotificationHelper", "SecurityException posting notification (pre-Tiramisu, unusual)", e)
            }
        }


    }
}