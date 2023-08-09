package io.acay.fpl.service

import android.os.Handler
import android.os.Looper
import io.acay.fpl.model.ClassF
import okhttp3.FormBody
import okhttp3.OkHttpClient
import org.json.JSONObject

class ClassListService {
    companion object {
        fun getClasses(t: String, q: String?, e: Int?, res: (ArrayList<ClassF>) -> Unit) {
            Thread {
                try {
                    fetch(t, q, e, res)
                } catch (e: Exception) {
                    // error
                }
            }.start()
        }

        private fun fetch(t: String, q: String?, e: Int?, res: (ArrayList<ClassF>) -> Unit) {
            val req =
                okhttp3.Request.Builder().url("http://acay.atwebpages.com/asm/api/getClasses.php")
                    .post(
                        FormBody.Builder()
                            .add("t", t)
                            .add("q", q ?: "")
                            .add("e", if (e == 0) "norm" else if (e == 1) "exam" else "").build()
                    ).build()

            OkHttpClient.Builder().build().newCall(req).execute().use {
                val body = it.body!!.string()
                val jsonArr = JSONObject(body).getJSONArray("data")

                val list = arrayListOf<ClassF>()
                for (i in 0 until jsonArr.length()) {
                    val e = jsonArr.get(i) as JSONObject

                    list.add(
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
                    res.invoke(list)
                }
            }
        }
    }
}