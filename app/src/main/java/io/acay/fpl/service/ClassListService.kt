package io.acay.fpl.service

import android.os.Handler
import android.os.Looper
import io.acay.fpl.model.ClassF
import okhttp3.OkHttpClient
import org.json.JSONObject

class ClassListService {
    companion object {
        fun getClasses(q: String?, res: (ArrayList<ClassF>) -> Unit) {
            Thread {
                try {
                    val req = okhttp3.Request.Builder()
                        .url("http://acay.atwebpages.com/asm/api/getClasses.php${if (q != null) "?q=$q" else ""}")
                        .get().build()

                    OkHttpClient.Builder().build().newCall(req).execute().use {
                        val jsonArr = JSONObject(it.body!!.string()).getJSONArray("data")

                        with(arrayListOf<ClassF>()) {
                            for (i in 0 until jsonArr.length()) {
                                val e = jsonArr.get(i) as JSONObject

                                add(
                                    ClassF(
                                        e.getInt("id"),
                                        e.getString("shift"),
                                        e.getString("date"),
                                        e.getString("teacher"),
                                        e.getString("subjectId"),
                                        e.getString("subjectName"),
                                        e.getString("room"),
                                        e.getString("onlineLink"),
                                        e.getString("location"),
                                        e.getString("periodFrom"),
                                        e.getString("periodTo"),
                                        e.getString("details")
                                    )
                                )
                            }

                            Handler(Looper.getMainLooper()).post {
                                res.invoke(this)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // error
                }
            }.start()
        }
    }
}