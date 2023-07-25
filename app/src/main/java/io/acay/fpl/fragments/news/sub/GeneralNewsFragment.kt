package io.acay.fpl.fragments.news.sub

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.acay.fpl.R
import io.acay.fpl.fragments.news.ArticleDrawerFragment
import io.acay.fpl.fragments.news.sub.adapter.GeneralNewsAdapter
import io.acay.fpl.model.Article
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.text.SimpleDateFormat

class GeneralNewsFragment : Fragment(R.layout.news_fragment_sub_general) {
    private lateinit var recyclerAdapter: GeneralNewsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getArticles()

        recyclerAdapter = GeneralNewsAdapter(arrayListOf())
        recyclerAdapter.onItemClick = {
            val dialog = ArticleDrawerFragment(it)
            dialog.show(childFragmentManager, null)
        }

        view.findViewById<RecyclerView>(R.id.fragment_news_sub_general_recycler)
            .let { recyclerView ->
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = recyclerAdapter
            }
    }

    private fun getArticles() {
        Thread {
            val req =
                okhttp3.Request.Builder().url("http://acay.atwebpages.com/asm/api/getPosts.php")
                    .get().build()

            OkHttpClient.Builder().build().newCall(req).execute().use {
                val jsonArr = JSONObject(it.body!!.string()).getJSONArray("data")

                with(arrayListOf<Article>()) {
                    for (i in 0..jsonArr.length()) {
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

                    recyclerAdapter.update(this)
                }
            }
        }.start()
    }
}