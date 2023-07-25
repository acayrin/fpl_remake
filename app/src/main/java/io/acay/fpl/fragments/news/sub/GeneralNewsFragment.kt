package io.acay.fpl.fragments.news.sub

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.acay.fpl.R
import io.acay.fpl.fragments.news.ArticleDrawerFragment
import io.acay.fpl.fragments.news.sub.adapter.GeneralNewsAdapter
import io.acay.fpl.model.Article

class GeneralNewsFragment : Fragment(R.layout.news_fragment_sub_general) {
    private val articleList = arrayListOf<Article>()
    private lateinit var recyclerAdapter: GeneralNewsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        for (i in 0..20) {
            articleList.add(
                Article(
                    i, "Title ${i}", "Author ${i}", System.currentTimeMillis(), "Content\n\n\n\n\n\nContent"
                )
            )
        }

        recyclerAdapter = GeneralNewsAdapter(articleList)
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
}