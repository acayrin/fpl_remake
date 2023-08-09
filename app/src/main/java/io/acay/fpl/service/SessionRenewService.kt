package io.acay.fpl.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import okhttp3.FormBody
import okhttp3.OkHttpClient
import org.json.JSONObject

class SessionRenewService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            while (true) {
                Thread.sleep(24 * 3_600 * 1_000)

                val req = okhttp3.Request.Builder()
                    .url("http://acay.atwebpages.com/asm/api/renewSession.php").post(
                        FormBody.Builder().add(
                            "t", getSharedPreferences("fpl_u", MODE_PRIVATE).getString("t", "")!!
                        ).build()
                    ).build()

                try {
                    OkHttpClient.Builder().build().newCall(req).execute().use {
                        val body = it.body!!.string()

                        val jsonObj = JSONObject(body)
                        getSharedPreferences("fpl_u", MODE_PRIVATE).edit()
                            .putString("t", jsonObj.getString("t"))
                    }
                } catch (e: Exception) {
                    Log.e("e", e.message!!)
                }
            }
        }.start()

        return START_REDELIVER_INTENT
    }
}