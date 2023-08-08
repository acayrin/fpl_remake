package io.acay.fpl.fragments.news

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.acay.fpl.R
import io.acay.fpl.model.Article
import java.text.SimpleDateFormat

class ArticleDrawerFragment(private val article: Article) :
    BottomSheetDialogFragment(R.layout.news_fragment_article_bottom_drawer) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val articleTitle: AppCompatTextView = view.findViewById(R.id.article_title)
        val articleAuthor: AppCompatTextView = view.findViewById(R.id.article_author)
        val articleTimestamp: AppCompatTextView = view.findViewById(R.id.article_timestamp)
        val articleContent: AppCompatTextView = view.findViewById(R.id.article_content)

        articleTitle.text = article.title
        articleAuthor.text = article.author
        articleTimestamp.text = article.timestamp
        articleContent.text = article.content
    }
}