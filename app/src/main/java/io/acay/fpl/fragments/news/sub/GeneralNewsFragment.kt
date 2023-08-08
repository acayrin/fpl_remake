package io.acay.fpl.fragments.news.sub

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.acay.fpl.R
import io.acay.fpl.fragments.news.ArticleDrawerFragment
import io.acay.fpl.fragments.news.sub.adapter.GeneralNewsAdapter
import io.acay.fpl.model.Article
import io.acay.fpl.service.LatestNewsService
import okhttp3.OkHttpClient
import org.json.JSONObject

class GeneralNewsFragment : Fragment(R.layout.news_fragment_sub_general) {
    private lateinit var recyclerAdapter: GeneralNewsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LatestNewsService.getArticles(null) {
            renderLatestArticle(it[0])
            recyclerAdapter.update(it)
        }

        recyclerAdapter = GeneralNewsAdapter(arrayListOf())
        recyclerAdapter.onItemClick = { article, viewHolder, viewType ->
            requireContext().getSharedPreferences("vs_${article.id}", Context.MODE_PRIVATE).edit()
                .putBoolean("viewed", true).apply()

            renderLatestArticle(article)
            renderTimelineEntry(viewHolder, viewType)
        }

        recyclerAdapter.onItemRendered = { article, viewHolder, viewType ->
            val viewState =
                requireContext().getSharedPreferences("vs_${article.id}", Context.MODE_PRIVATE)
                    .getBoolean("viewed", false)

            if (viewState) renderTimelineEntry(viewHolder, viewType)
        }

        view.findViewById<RecyclerView>(R.id.fragment_news_sub_general_timeline)
            .let { recyclerView ->
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = recyclerAdapter
            }
    }

    private fun renderTimelineEntry(viewHolder: GeneralNewsAdapter.ViewHolder, viewType: Int) {
        with(viewHolder.timelineView) {
            setStartLineColor(
                resources.getColor(R.color.background_light, null), viewType
            )
            setEndLineColor(
                resources.getColor(R.color.background_light, null), viewType
            )
            marker = ColorDrawable(resources.getColor(R.color.background_light, null))
        }

        viewHolder.entryTimestamp.setTextColor(
            resources.getColor(
                R.color.primary, null
            )
        )

        viewHolder.entryTimestamp.background = ResourcesCompat.getDrawable(
            resources, R.drawable.badge_timeline_date_seen, null
        )
    }

    @SuppressLint("SetTextI18n")
    private fun renderLatestArticle(article: Article) {
        val parentView = parentFragment?.view ?: return

        parentView.findViewById<LinearLayoutCompat>(R.id.fragment_news_latest).apply {
            val title = findViewById<AppCompatTextView>(R.id.fragment_news_latest_title)
            val author = findViewById<AppCompatTextView>(R.id.fragment_news_latest_author)
            val timestamp = findViewById<AppCompatTextView>(R.id.fragment_news_latest_timestamp)
            val content = findViewById<AppCompatTextView>(R.id.fragment_news_latest_content)
            val readMore = findViewById<AppCompatButton>(R.id.fragment_news_latest_readmore)

            title.apply { text = article.title }
            author.apply { text = article.author }
            timestamp.apply { text = article.timestamp }
            content.apply {
                with(article.content.split(" ")) {
                    if (size > 30) {
                        text = subList(0, 30).joinToString(" ")
                        text = "${text}..."
                        readMore.visibility = View.VISIBLE
                    } else {
                        text = joinToString(" ")
                        readMore.visibility = View.GONE
                    }
                }
            }

            readMore.setOnClickListener {
                val dialog = ArticleDrawerFragment(article)
                dialog.show(childFragmentManager, null)
            }
        }
    }
}