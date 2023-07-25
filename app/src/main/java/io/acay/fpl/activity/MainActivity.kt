package io.acay.fpl.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import io.acay.fpl.R
import io.acay.fpl.fragments.classes.ClassesFragment
import io.acay.fpl.fragments.news.NewsFragment
import io.acay.fpl.fragments.settings.SettingsFragment
import io.acay.fpl.hooks.StartServices

class MainActivity : AppCompatActivity() {
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private val tabs = arrayOf(
        Pair(NewsFragment(), R.drawable.vec_newspaper),
        Pair(ClassesFragment(), R.drawable.vec_google_classroom),
        Pair(SettingsFragment(), R.drawable.vec_cogwheel)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // services
        StartServices.I.startServices(this)

        viewPager = findViewById(R.id.main_view_pager)
        viewPager.adapter = PagerAdapter(supportFragmentManager, tabs)

        tabLayout = findViewById(R.id.main_tab_layout)
        tabLayout.let {
            it.setupWithViewPager(viewPager)
            it.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    setTabColor(tab, R.color.background)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    setTabColor(tab, R.color.primary)
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    setTabColor(tab, R.color.background)
                }
            })

            // remove blank tabs and re-add all of them
            it.removeAllTabs()
            for (p in tabs) {
                ResourcesCompat.getDrawable(resources, p.second, null)!!.let { draw ->
                    draw.setTint(resources.getColor(R.color.primary))

                    val iv = AppCompatImageView(this)
                    iv.setImageDrawable(draw)

                    val tab = it.newTab()
                    tab.customView = iv
                    it.addTab(tab)
                }
            }
        }
    }

    private fun setTabColor(tab: TabLayout.Tab?, color: Int) {
        if (tab != null) {
            (tab.customView as AppCompatImageView).drawable.setTint(
                ResourcesCompat.getColor(
                    resources, color, null
                )
            )
        }
    }

    class PagerAdapter(fm: FragmentManager, private val tabs: Array<Pair<Fragment, Int>>) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = tabs.size
        override fun getItem(position: Int): Fragment = tabs[position].first
    }
}