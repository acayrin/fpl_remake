package io.acay.fpl.fragments.classes.sub.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import io.acay.fpl.R
import io.acay.fpl.model.Article
import io.acay.fpl.model.ClassF

class UpcomingClassesAdapter(private val list: ArrayList<ClassF>) :
    RecyclerView.Adapter<UpcomingClassesAdapter.ViewHolder>() {
    var onItemClick: ((Article) -> Unit)? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateWithPeriod: AppCompatTextView =
            itemView.findViewById(R.id.classes_fragment_card_date)
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

        val expandableContent: LinearLayoutCompat =
            itemView.findViewById(R.id.classes_fragment_card_expandable_content)
        val clickableContent: LinearLayoutCompat =
            itemView.findViewById(R.id.classes_fragment_card_base_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.classes_fragment_recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val c = list[position]

        holder.dateWithPeriod.text = "${c.date} - Ca ${c.period}"
        holder.room.text = c.onlineLink ?: c.room
        holder.subjectName.text = c.subjectName

        // expandable content
        holder.location.text = c.location
        holder.duration.text = "${c.durationFrom} - ${c.durationTo}"
        holder.teacher.text = c.teacher
        holder.subjectId.text = c.subjectId
        holder.onlineLink.text = c.onlineLink
        holder.details.text = c.details

        holder.clickableContent.setOnClickListener {
            holder.expandableContent.visibility =
                if (holder.expandableContent.visibility == View.GONE) View.VISIBLE else View.GONE
        }
    }
}