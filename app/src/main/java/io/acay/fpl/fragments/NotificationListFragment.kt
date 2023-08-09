package io.acay.fpl.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.acay.fpl.R
import io.acay.fpl.model.sqlite.Notification
import io.acay.fpl.service.sqlite.NotificationStore
import java.util.Locale


class NotificationListFragment(var runAfterSeenAll: (() -> Unit)?) :
    BottomSheetDialogFragment(R.layout.notification_fragment_list) {
    private lateinit var list: ArrayList<Notification>
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: NotificationListAdapter
    private lateinit var notificationStore: NotificationStore
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (context != null) {
            notificationStore = NotificationStore(requireContext())

            list = NotificationStore(requireContext()).getNotifications()

            recyclerAdapter = NotificationListAdapter()
            recyclerView = view.findViewById(R.id.fragment_notification_recycler)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = recyclerAdapter
        }

        view.findViewById<AppCompatButton>(R.id.fragment_notification_read_all).setOnClickListener {
            if (notificationStore.setAllAsSeen()) {
                list = NotificationStore(requireContext()).getNotifications()
                recyclerAdapter.notifyItemRangeChanged(0, list.size)

                runAfterSeenAll?.invoke()
            }
        }
    }

    inner class NotificationListAdapter :
        RecyclerView.Adapter<NotificationListAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val title: AppCompatTextView = itemView.findViewById(R.id.notification_item_title)
            val description: AppCompatTextView =
                itemView.findViewById(R.id.notification_item_description)
            val timestamp: AppCompatTextView =
                itemView.findViewById(R.id.notification_item_timestamp)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.notification_fragment_recycler_item, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entry = list[position]

            if (!entry.seen) {
                holder.title.setTypeface(null, Typeface.BOLD_ITALIC)
                holder.description.setTypeface(null, Typeface.BOLD)
                holder.timestamp.setTypeface(null, Typeface.BOLD)
            } else {
                holder.title.setTypeface(null, Typeface.NORMAL)
                holder.description.setTypeface(null, Typeface.NORMAL)
                holder.timestamp.setTypeface(null, Typeface.NORMAL)
            }

            holder.itemView.setOnClickListener {
                if (notificationStore.setAsSeen(entry.id)) {
                    list[position].seen = true
                    notifyItemChanged(position)
                }
            }

            holder.title.text = entry.title
            holder.description.text = entry.description
            holder.timestamp.text =
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .parse(entry.timestamp)?.let { DateUtils.getRelativeTimeSpanString(it.time) }
        }
    }
}