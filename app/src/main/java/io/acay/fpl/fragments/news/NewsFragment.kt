package io.acay.fpl.fragments.news

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
import io.acay.fpl.fragments.news.sub.GeneralNewsFragment

class NewsFragment : Fragment(R.layout.news_fragment) {
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private val tabs = arrayOf<Pair<Fragment, String>>(
        Pair(GeneralNewsFragment(), "General"),
        Pair(GeneralNewsFragment(), "Something"),
        Pair(GeneralNewsFragment(), "Else"),
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = view.findViewById(R.id.fragment_news_view_pager)
        viewPager.adapter = PagerAdapter(childFragmentManager, tabs)

        tabLayout = view.findViewById(R.id.fragment_news_tab_layout)
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