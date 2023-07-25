package io.acay.fpl.fragments.news.sub.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import io.acay.fpl.R
import io.acay.fpl.model.Article
import java.text.SimpleDateFormat

class GeneralNewsAdapter(private val list: ArrayList<Article>) : RecyclerView.Adapter<GeneralNewsAdapter.ViewHolder>() {
    var onItemClick: ((Article) -> Unit)? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val entryTitle: AppCompatTextView = itemView.findViewById(R.id.fragment_news_item_title)
        val entryAuthor: AppCompatTextView = itemView.findViewById(R.id.fragment_news_item_author)
        val entryTimestamp: AppCompatTextView = itemView.findViewById(R.id.fragment_news_item_timestamp)
        val entryContent: AppCompatTextView = itemView.findViewById(R.id.fragment_news_item_content)
        val entryReadMore: AppCompatButton = itemView.findViewById(R.id.fragment_news_item_read_more)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.news_fragment_recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = list[position]

        holder.entryTitle.text = entry.title
        holder.entryAuthor.text = entry.author
        holder.entryTimestamp.text = SimpleDateFormat().format(entry.timestamp)
        holder.entryContent.text = entry.content
        holder.entryReadMore.setOnClickListener {
            onItemClick?.invoke(entry)
        }
    }
}