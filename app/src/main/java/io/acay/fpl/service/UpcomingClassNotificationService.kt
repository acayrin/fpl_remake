package io.acay.fpl.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import io.acay.fpl.R
import io.acay.fpl.model.sqlite.Notification
import io.acay.fpl.service.sqlite.NotificationStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UpcomingClassNotificationService : Service() {
    private val id = 1337
    private val delay = 300 * 1_000L
    private val notificationChannel = NotificationChannel(
        id.toString(), id.toString(), NotificationManager.IMPORTANCE_LOW
    )

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // TODO implements an API pull that checks for upcoming classes and send notification about them

        Thread {
            while (true) {
                val value = Notification(
                    0,
                    "Test notification",
                    "Test notification description",
                    SimpleDateFormat("mm-dd-yyyy HH:mm:ss", Locale.US).format(
                        Date()
                    ).toString(),
                    false
                )

                // store it in store
                NotificationStore(applicationContext).insertNotification(value)

                // check
                if (application.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    Log.w("FPL_Notification", "Permission denied for POST_NOTIFICATIONS")
                    Thread.sleep(delay)
                    continue
                }

                // create base notification object
                val notification = NotificationCompat.Builder(applicationContext, id.toString())
                    .setSmallIcon(R.drawable.ico_app_main).setContentTitle(value.title)
                    .setContentText(value.description)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT).setStyle(
                        // the text to be showed when expanding the notification bubble
                        NotificationCompat.BigTextStyle().bigText(value.description)
                    ).setContentIntent(
                        // backward compatibility
                        PendingIntent.getActivity(
                            applicationContext, 0, Intent(), PendingIntent.FLAG_IMMUTABLE
                        )
                    ).build()
                notification.flags = NotificationCompat.FLAG_AUTO_CANCEL

                // send to the user
                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(notificationChannel)
                notificationManager.notify(id, notification)

                Thread.sleep(delay)
            }
        }.start()

        return START_REDELIVER_INTENT
    }
}