package io.acay.fpl.fragments.classes.sub.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.vipulasri.timelineview.TimelineView
import io.acay.fpl.R
import io.acay.fpl.model.ClassF
import java.util.Locale

class UpcomingClassesAdapter(private val list: ArrayList<ClassF>) :
    RecyclerView.Adapter<UpcomingClassesAdapter.ViewHolder>() {
    var onItemClick: ((ClassF) -> Unit)? = null
    var onItemRendered: ((ClassF, ViewHolder, Int) -> Unit)? = null

    class ViewHolder(itemView: View, val viewType: Int) : RecyclerView.ViewHolder(itemView) {
        var timelineView: TimelineView

        init {
            timelineView = itemView.findViewById(R.id.timeline)
            timelineView.initLine(viewType)
        }

        val period: AppCompatTextView = itemView.findViewById(R.id.classes_fragment_card_date)
        val timestamp: AppCompatTextView =
            itemView.findViewById(R.id.classes_fragment_card_subject_timestamp)
        val room: AppCompatTextView = itemView.findViewById(R.id.classes_fragment_card_room)
        val subjectName: AppCompatTextView =
            itemView.findViewById(R.id.classes_fragment_card_subject_name)
        val subjectId: AppCompatTextView =
            itemView.findViewById(R.id.classes_fragment_card_subject_id)
        val location: AppCompatTextView = itemView.findViewById(R.id.classes_fragment_card_location)
        val teacher: AppCompatTextView = itemView.findViewById(R.id.classes_fragment_card_teacher)
        val duration: AppCompatTextView = itemView.findViewById(R.id.classes_fragment_card_duration)
        val onlineLink: AppCompatTextView =
            itemView.findViewById(R.id.classes_fragment_card_online_link)
        val details: AppCompatTextView = itemView.findViewById(R.id.classes_fragment_card_details)
        val selfNote: AppCompatEditText = itemView.findViewById(R.id.classes_fragment_card_local_note)

        val expandableContent: LinearLayoutCompat =
            itemView.findViewById(R.id.classes_fragment_card_expandable_content)
        val clickableContent: LinearLayoutCompat =
            itemView.findViewById(R.id.classes_fragment_card_base_content)
    }

    fun update(new: ArrayList<ClassF>) {
        list.clear()
        list.addAll(new)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.classes_fragment_recycler_item, parent, false)
        return ViewHolder(view, viewType)
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int
        = TimelineView.getTimeLineViewType(position, itemCount)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val c = list[position]

        holder.period.text = "Shift ${c.shift}"
        holder.room.text = c.onlineLink?.takeIf { s -> s.isNotEmpty() && s != "null"} ?: c.room
        holder.subjectName.text = c.subjectName
        holder.timestamp.text =
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(c.date)?.let {
                DateUtils.getRelativeTimeSpanString(it.time)
            }

        // expandable content
        holder.location.text = c.location
        holder.duration.text = "${c.periodFrom} - ${c.periodTo}"
        holder.teacher.text = c.teacher
        holder.subjectId.text = c.subjectId
        holder.onlineLink.text = c.onlineLink?.takeIf { s -> s.isNotEmpty() && s != "null"} ?: ""
        holder.details.text = c.details?.takeIf { s -> s.isNotEmpty() && s != "null"} ?: ""

        holder.clickableContent.setOnClickListener {
            holder.expandableContent.visibility =
                if (holder.expandableContent.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        onItemRendered?.invoke(c, holder, holder.viewType)
    }
}