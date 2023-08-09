package io.acay.fpl.fragments.classes.sub

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.acay.fpl.R
import io.acay.fpl.fragments.classes.sub.adapter.UpcomingClassesAdapter
import io.acay.fpl.model.ClassF
import io.acay.fpl.service.ClassListService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UpcomingExamsFragment : Fragment(R.layout.classes_fragment_sub_upcoming), ClassFragment {
    private val classList = arrayListOf<ClassF>()
    private lateinit var recyclerAdapter: UpcomingClassesAdapter
    private lateinit var t: String

    override fun commitSearch(clause: String?) {
        ClassListService.getClasses(t, clause, 1) {
            classList.clear()
            classList.addAll(it)
            recyclerAdapter.update(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (context != null) {
            t = requireContext().getSharedPreferences(
                "fpl_u", Context.MODE_PRIVATE
            ).getString("t", "")!!
        }

        recyclerAdapter = UpcomingClassesAdapter(classList)
        val rv = view.findViewById<RecyclerView>(R.id.classes_fragment_sub_upcoming_recycler)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = recyclerAdapter

        ClassListService.getClasses(t, null, 1) {
            classList.addAll(it)
            recyclerAdapter.update(it)

            classList.indexOfFirst { cl ->
                SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.US
                ).parse(cl.date)!!.time >= Date().time
            }.apply { rv.scrollToPosition(this) }

            recyclerAdapter.onItemRendered = { c, viewHolder, viewType ->
                val sp = requireContext().getSharedPreferences("cn_${c.id}", Context.MODE_PRIVATE)
                val before = SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.US
                ).parse(c.date)!!.time < Date().time

                with(viewHolder.timelineView) {
                    val color = resources.getColor(
                        if (before) R.color.background_light else R.color.primary, null
                    )

                    setStartLineColor(color, viewType)
                    setEndLineColor(color, viewType)
                    marker = ColorDrawable(color)
                }

                viewHolder.timestamp.setTextColor(
                    resources.getColor(
                        if (before) R.color.primary else R.color.background, null
                    )
                )

                viewHolder.timestamp.background = ResourcesCompat.getDrawable(
                    resources, if (before) R.drawable.badge_timeline_date_seen
                    else R.drawable.badge_timeline_date, null
                )

                if (!sp.getString("content", null).isNullOrEmpty()) {
                    viewHolder.timestamp.text = "${viewHolder.timestamp.text}\n(noted)"
                } else {
                    viewHolder.timestamp.text =
                        viewHolder.timestamp.text.replace(Regex("\n(noted)"), "")
                }

                viewHolder.selfNote.apply {
                    setText(sp.getString("content", null))

                    doOnTextChanged { text, _, _, _ ->
                        sp.edit().putString("content", text.toString()).apply()
                    }
                }
            }
        }
    }
}