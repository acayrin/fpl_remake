package io.acay.fpl.fragments.classes

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
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
    private lateinit var tabs: Array<Pair<Fragment, String>>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabs = arrayOf(
            Pair(UpcomingClassesFragment(), getString(R.string.classes_fragment_tab_upcoming)),
            Pair(UpcomingClassesFragment(), getString(R.string.classes_fragment_tab_exams))
        )

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

    private class PagerAdapter(
        fm: FragmentManager, private val tabs: Array<Pair<Fragment, String>>
    ) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = tabs.size
        override fun getItem(position: Int): Fragment = tabs[position].first
    }
}