package io.acay.fpl.fragments.news.sub.adapter

import android.text.format.DateFormat
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.github.vipulasri.timelineview.TimelineView
import io.acay.fpl.R
import io.acay.fpl.model.Article
import java.util.Locale

class GeneralNewsAdapter(private val list: ArrayList<Article>) :
    RecyclerView.Adapter<GeneralNewsAdapter.ViewHolder>() {
    var onItemClick: ((Article, ViewHolder, Int) -> Unit)? = null
    var onItemRendered: ((Article, ViewHolder, Int) -> Unit)? = null

    class ViewHolder(itemView: View, val viewType: Int) : RecyclerView.ViewHolder(itemView) {
        var timelineView: TimelineView

        init {
            timelineView = itemView.findViewById(R.id.timeline)
            timelineView.initLine(viewType)
        }

        val entryTitle: AppCompatTextView = itemView.findViewById(R.id.fragment_news_item_title)
        val entryAuthor: AppCompatTextView = itemView.findViewById(R.id.fragment_news_item_author)
        val entryTimestamp: AppCompatTextView =
            itemView.findViewById(R.id.fragment_news_item_timestamp)
//        val entryContent: AppCompatTextView = itemView.findViewById(R.id.fragment_news_item_content)
//        val entryReadMore: AppCompatButton =
//            itemView.findViewById(R.id.fragment_news_item_read_more)
    }

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.news_fragment_recycler_item, parent, false)
        return ViewHolder(view, viewType)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = list[position]

        holder.entryTitle.text = entry.title
        holder.entryAuthor.text = entry.author
        holder.entryTimestamp.text =
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(entry.timestamp)
                ?.let {
                    DateUtils.getRelativeTimeSpanString(
                        it.time
                    )
                }
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(entry, holder, holder.viewType)
        }

        onItemRendered?.invoke(entry, holder, holder.viewType)
//        holder.entryContent.text = entry.content
//        holder.entryReadMore.setOnClickListener {
//            onItemClick?.invoke(entry)
//        }
    }

    fun update(articleList: ArrayList<Article>) {
        this.list.clear()
        this.list.addAll(articleList)
        notifyDataSetChanged()
    }
}