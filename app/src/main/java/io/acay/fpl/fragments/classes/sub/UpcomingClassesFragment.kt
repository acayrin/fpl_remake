package io.acay.fpl.fragments.classes.sub

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
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
import java.util.Random

class UpcomingClassesFragment : Fragment(R.layout.classes_fragment_sub_upcoming) {
    private val classList = ClassListService.getClasses()
    private lateinit var recyclerAdapter: UpcomingClassesAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<RecyclerView>(R.id.classes_fragment_sub_upcoming_recycler)
            .let { recyclerView ->
                recyclerAdapter = UpcomingClassesAdapter(classList)

                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = recyclerAdapter

                val pos = classList.indexOfFirst {
                    SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss", Locale.US
                    ).parse(it.date)!!.time >= Date().time
                }
                recyclerView.scrollToPosition(pos)

                recyclerAdapter.onItemRendered = { c, viewHolder, viewType ->
                    val sp = requireContext().getSharedPreferences("cn_${c.id}", Context.MODE_PRIVATE)
                    val before = SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss", Locale.US
                    ).parse(c.date)!!.time < Date().time

                    with(viewHolder.timelineView) {
                        setStartLineColor(
                            resources.getColor(
                                if (before) R.color.background_light else R.color.primary, null
                            ), viewType
                        )
                        setEndLineColor(
                            resources.getColor(
                                if (before) R.color.background_light else R.color.primary, null
                            ), viewType
                        )
                        marker = ColorDrawable(
                            resources.getColor(
                                if (before) R.color.background_light else R.color.primary, null
                            )
                        )
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
                        viewHolder.timestamp.text = viewHolder.timestamp.text.replace(Regex("\n(noted)"), "")
                    }

                    viewHolder.selfNote.apply {
                        if (context == null) return@apply

                        setText(sp.getString("content", null))

                        doOnTextChanged { text, _, _, _ ->
                            sp.edit().putString("content", text.toString()).apply()
                        }
                    }
                }
            }
    }
}