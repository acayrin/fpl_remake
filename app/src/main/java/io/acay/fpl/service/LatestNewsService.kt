package io.acay.fpl.service

import android.os.Handler
import android.os.Looper
import io.acay.fpl.model.Article
import okhttp3.OkHttpClient
import org.json.JSONObject

class LatestNewsService {
    companion object {
        fun getArticles(type: Int?, res: (ArrayList<Article>) -> Unit) {
            Thread {
                try {
                    val req =
                        okhttp3.Request.Builder()
                            .url("http://acay.atwebpages.com/asm/api/getPosts.php?t=${type ?: 0}")
                            .get().build()

                    OkHttpClient.Builder().build().newCall(req).execute().use {
                        val jsonArr = JSONObject(it.body!!.string()).getJSONArray("data")

                        with(arrayListOf<Article>()) {
                            for (i in 0 until jsonArr.length()) {
                                val article = jsonArr.get(i) as JSONObject

                                add(
                                    Article(
                                        article.getInt("id"),
                                        article.getString("title"),
                                        article.getString("author"),
                                        article.getString("timestamp"),
                                        article.getString("content")
                                    )
                                )
                            }

                            Handler(Looper.getMainLooper()).post {
                                res.invoke(this)
                            }
                        }
                    }
                } catch(e: Exception) {
                    // error
                }
            }.start()
        }
    }
}