package io.acay.fpl.fragments.classes

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.MultiAutoCompleteTextView
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import io.acay.fpl.R
import io.acay.fpl.fragments.classes.sub.ClassFragment
import io.acay.fpl.fragments.classes.sub.UpcomingClassesFragment
import io.acay.fpl.fragments.classes.sub.UpcomingExamsFragment
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ClassesFragment : Fragment(R.layout.classes_fragment) {
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var searchBox: AppCompatMultiAutoCompleteTextView
    private lateinit var tabs: Array<Pair<Fragment, String>>

    private var handler: Handler = Handler(Looper.getMainLooper())
    private var workRunnable: Runnable = Runnable { }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabs = arrayOf(
            Pair(UpcomingClassesFragment(), getString(R.string.classes_fragment_tab_upcoming)),
            Pair(UpcomingExamsFragment(), getString(R.string.classes_fragment_tab_exams))
        )

        searchBox = view.findViewById(R.id.classes_fragment_search_box)
        with(searchBox) {
            threshold = 2
            setTokenizer(SpaceTokenizer())
            setAdapter(
                ArrayAdapter(context,
                    android.R.layout.select_dialog_item,
                    searchKeys.map { "${it.first}:" })
            )

            doOnTextChanged { text, _, _, _ ->
                handler.removeCallbacks(workRunnable)
                workRunnable = Runnable { commitSearch(text) }
                handler.postDelayed(workRunnable, 750)
            }
        }

        viewPager = view.findViewById(R.id.classes_fragment_tab_layout_view_pager)
        viewPager.adapter = PagerAdapter(childFragmentManager, tabs)

        tabLayout = view.findViewById(R.id.classes_fragment_tab_layout)
        tabLayout.let { tabLayout ->
            tabLayout.setupWithViewPager(viewPager)

            tabs.forEach { tab ->
                with(AppCompatTextView(requireContext())) {
                    text = tab.second
                    textSize = 16F
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    setTextColor(ResourcesCompat.getColor(resources, R.color.primary, null))
                    tabLayout.getTabAt(tabs.indexOf(tab))!!.customView = this
                }
            }
        }
    }

    private class PagerAdapter(
        fm: FragmentManager, private val tabs: Array<Pair<Fragment, String>>
    ) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = tabs.size
        override fun getItem(position: Int): Fragment = tabs[position].first
    }

    private fun commitSearch(query: CharSequence?) {
        (viewPager.adapter?.instantiateItem(
            viewPager, viewPager.currentItem
        ) as ClassFragment).apply {
            commitSearch(
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
        Pair("year", "YEAR(fc.date)"),
        Pair("date", "fc.date"),

        Pair("subject", "fs.name"),
        Pair("subjectId", "fc.subjectId"),
        Pair("shift", "fc.shift"),
        Pair("room", "fc.room"),
        Pair("location", "fl.name"),
        Pair("teacher", "fc.teacher"),
        Pair("details", "fc.details"),
        Pair("onlineLink", "fc.onlineLink"),
        Pair("periodFrom", "ft.periodFrom"),
        Pair("periodTo", "ft.periodTo")
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
        val queries = "(?:[^\\s\"]+:\\s?+((\"[^\"]*\")|(\\S+)))|\\S+".toRegex().findAll(query)
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
                val key = s[0].trim()
                // the value to compare
                val value = s.subList(1, s.size).joinToString(":").trim()

                // if the key is valid then add to the pair list
                searchKeys.firstOrNull { k -> k.first.equals(key, true) }?.apply {
                    b.add(Pair(first, value.replace("\"", "")))
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
                val field = searchKeys.find { z -> z.first == it.first }!!.second

                val isNegate = it.second.indexOf("!") in 0..1
                val isAlternate = it.second.indexOf("?") in 0..1
                var value = it.second.replace(Regex("[!?]"), "")

                // if the current pair isn't the last, append (AND)
                if (query.first.indexOf(it) != 0) q += if (isAlternate) " OR " else " AND "

                if (it.first in arrayOf("day", "month", "date")) {
                    // special case querying dates
                    val comparator = if (Regex("[+>]").find(it.second) != null) {
                        if (Regex("=").find(it.second) != null) {
                            ">="
                        } else {
                            ">"
                        }
                    } else if (Regex("[-<]").find(it.second) != null) {
                        if (Regex("=").find(it.second) != null) {
                            "<="
                        } else {
                            "<"
                        }
                    } else "="

                    // cleaned value
                    value = it.second.replace(Regex("[><\\-=+]"), "")

                    // this only applies when the key is (date)
                    if (it.first == "date") {
                        // try to parse the date, following the dd/MM/yyyy format
                        var date: Date? = null
                        try {
                            date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(value)
                        } catch (_: ParseException) {
                            try {
                                date = SimpleDateFormat("dd/MM", Locale.getDefault()).parse(value)
                                date.year = Calendar.getInstance().get(Calendar.YEAR) - 1900
                            } catch (_: ParseException) {
                                try {
                                    date = SimpleDateFormat("dd", Locale.getDefault()).parse(value)
                                    date.year = Calendar.getInstance().get(Calendar.YEAR) - 1900
                                    date.month = Calendar.getInstance().get(Calendar.MONTH)
                                } catch (_: ParseException) {
                                    // ignore
                                }
                            }
                        }

                        // set the correct value to compare with database
                        if (date != null) {
                            value = "${date.year + 1900}-${date.month + 1}-${date.date}"
                        }
                    }

                    q += "$field $comparator \"$value\""
                } else {
                    // append query (<key> LIKE "%<value>%")
                    q += "$field ${if (isNegate) "NOT " else ""}LIKE \"%$value%\""
                }
            }

            // append additional clauses using excessive queries
            if (second.isNotEmpty()) {
                // append AND if key search is requested
                if (first.isNotEmpty()) q += " AND "
                q += "(" // start grouping

                // loop through all the keys
                conditionalKeys.forEach { k ->
                    // append query (<key> LIKE "%<value>%")
                    q += "$k LIKE \"%$second%\""

                    // if the current pair isn't the last, append (OR)
                    if (conditionalKeys.indexOf(k) != conditionalKeys.size - 1) q += " OR "
                }

                q += ")" // close the grouping
            }
        }

        Log.i("A", q!!)
        return q // return the (WHERE) clause
    }

    inner class SpaceTokenizer : MultiAutoCompleteTextView.Tokenizer {
        private val i = 0
        override fun findTokenStart(inputText: CharSequence, cursor: Int): Int {
            var idx = cursor
            while (idx > 0 && inputText[idx - 1] != ' ') {
                idx--
            }
            while (idx < cursor && inputText[idx] == ' ') {
                idx++
            }
            return idx
        }

        override fun findTokenEnd(inputText: CharSequence, cursor: Int): Int {
            var idx = cursor
            val length = inputText.length
            while (idx < length) {
                if (inputText[i] == ' ') {
                    return idx
                } else {
                    idx++
                }
            }
            return length
        }

        override fun terminateToken(inputText: CharSequence): CharSequence {
            var idx = inputText.length
            while (idx > 0 && inputText[idx - 1] == ' ') {
                idx--
            }
            return if (idx > 0 && inputText[idx - 1] == ' ') {
                inputText
            } else {
                if (inputText is Spanned) {
                    val sp = SpannableString("$inputText ")
                    TextUtils.copySpansFrom(inputText, 0, inputText.length, Any::class.java, sp, 0)
                    sp
                } else {
                    "$inputText "
                }
            }
        }
    }
}