package io.acay.fpl.fragments.classes

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import io.acay.fpl.R
import io.acay.fpl.fragments.classes.sub.UpcomingClassesFragment


class ClassesFragment : Fragment(R.layout.classes_fragment) {
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var searchBox: AppCompatEditText
    private lateinit var tabs: Array<Pair<Fragment, String>>

    private var handler: Handler = Handler(Looper.getMainLooper())
    private var workRunnable: Runnable = Runnable { }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabs = arrayOf(
            Pair(UpcomingClassesFragment(), getString(R.string.classes_fragment_tab_upcoming)),
            Pair(UpcomingClassesFragment(), getString(R.string.classes_fragment_tab_exams))
        )

        searchBox = view.findViewById(R.id.classes_fragment_search_box)
        searchBox.doOnTextChanged { text, _, _, _ ->
            handler.removeCallbacks(workRunnable)
            workRunnable = Runnable { commitSearch(text) }
            handler.postDelayed(workRunnable, 500)
        }

        viewPager = view.findViewById(R.id.classes_fragment_tab_layout_view_pager)
        viewPager.adapter = PagerAdapter(childFragmentManager, tabs)

        tabLayout = view.findViewById(R.id.classes_fragment_tab_layout)
        tabLayout.let {
            it.setupWithViewPager(viewPager)

            for (p in tabs) {
                val v = AppCompatTextView(requireContext())
                v.text = p.second
                v.textSize = 16F
                v.textAlignment = View.TEXT_ALIGNMENT_CENTER
                v.setTextColor(ResourcesCompat.getColor(resources, R.color.primary, null))
                it.getTabAt(tabs.indexOf(p))!!.customView = v
            }
        }
    }

    private fun commitSearch(query: CharSequence?) {
        viewPager.adapter?.instantiateItem(viewPager, viewPager.currentItem).apply {
            val frag = this as UpcomingClassesFragment

            frag.commitSearch(
                if (query.isNullOrEmpty()) null
                else Base64.encodeToString(
                    generateWhereClause(
                        generateSearchParams(query.toString())
                    )?.toByteArray(), Base64.DEFAULT
                )
            )
        }
    }

    private val searchKeys = arrayOf(
        Pair("day", "DAY(fc.date)"),
        Pair("month", "MONTH(fc.date)"),
        Pair("date", "fc.date"),

        Pair("subject", "fs.name"),
        Pair("subjectId", "fc.subjectId"),
        Pair("shift", "fc.shift"),
        Pair("room", "fc.room"),
        Pair("location", "fl.name"),
        Pair("teacher", "fc.teacher"),
    )
    private val conditionalKeys = arrayOf(
        "fc.shift",
        "fc.date",
        "fc.teacher",
        "fc.subjectId",
        "fs.name",
        "fc.room",
        "fc.onlineLink",
        "fl.name",
        "ft.periodFrom",
        "ft.periodTo",
        "fc.details"
    )

    private fun generateSearchParams(query: String): Pair<ArrayList<Pair<String, String>>, String> {
        // break down the query using regex
        // suppose to match either
        // 1. <string>:<string>
        // 2. <string>:"<string1> <string2>"
        // 3. <standalone_string>
        val queries = "(?:[^\\s\"]+:+((\"[^\"]*\")|(\\S+)))|\\S+".toRegex().findAll(query)
            .map { return@map it.value }

        // an array list of string pairs <key, value>
        val b = arrayListOf<Pair<String, String>>()
        // excessive string from query
        var z = ""

        // loop through the queries
        for (i in 0 until queries.count()) {
            val e = queries.elementAt(i)

            // if field matching is required by looking for ":"
            if (e.indexOf(":") != -1) {
                val s = e.split(":")

                // the key to look up
                val key = s[0]
                // the value to compare
                val value = s.subList(1, s.size).joinToString(":")

                // if the key is valid then add to the pair list
                searchKeys.firstOrNull { k -> k.first.equals(key, true) }?.apply {
                    b.add(Pair(second, value.replace("\"", "")))
                }
            } else {
                // else concat the string to the excessive one
                z += "$e "
            }
        }

        // remove trailing whitespaces
        z = z.trim()

        // return the results
        return Pair(b, z)
    }

    // TODO: since the query building is done on the client side, it does raise some security concerns
    private fun generateWhereClause(query: Pair<ArrayList<Pair<String, String>>, String>?): String? {
        var q: String? = null // base where query

        query?.apply {
            q = "WHERE "

            // loop through list of key:value
            first.forEach {
                // append query (<key> LIKE "%<value>%")
                q += "${it.first} LIKE \"%${it.second}%\""

                // if the current pair isn't the last, append (AND)
                if (query.first.indexOf(it) != query.first.size - 1) q += " AND "
            }

            // append additional clauses using excessive queries
            if (second.isNotEmpty()) {
                // append AND if key search is requested with parentheses for grouping
                if (first.isNotEmpty()) q += " AND ("

                // loop through all the keys
                conditionalKeys.forEach { k ->
                    // append query (<key> LIKE "%<value>%")
                    q += "$k LIKE \"%$second%\""

                    // if the current pair isn't the last, append (OR)
                    if (conditionalKeys.indexOf(k) != conditionalKeys.size - 1) q += " OR "
                }

                if (first.isNotEmpty()) q += ")" // close the grouping
            }
        }

        return q // return the (WHERE) clause
    }

    private class PagerAdapter(
        fm: FragmentManager, private val tabs: Array<Pair<Fragment, String>>
    ) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = tabs.size
        override fun getItem(position: Int): Fragment = tabs[position].first
    }
}