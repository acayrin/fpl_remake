package io.acay.fpl.hooks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import io.acay.fpl.service.UpcomingClassNotificationService

class StartServices {
    object I {
        fun startServices(a: AppCompatActivity) {
            a.startService(Intent(a, UpcomingClassNotificationService::class.java))
        }
    }
}