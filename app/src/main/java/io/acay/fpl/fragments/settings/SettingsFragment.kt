package io.acay.fpl.fragments.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.vipulasri.timelineview.TimelineView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.squareup.picasso.Picasso
import io.acay.fpl.R
import io.acay.fpl.activity.AuthActivity
import io.acay.fpl.fragments.notes.NotesFragment
import java.util.Calendar

class SettingsFragment : Fragment(R.layout.settings_fragment) {
    private lateinit var iAvatar: AppCompatImageView
    private lateinit var bName: AppCompatTextView
    private lateinit var bEmail: AppCompatTextView
    private lateinit var bSemester: AppCompatTextView
    private lateinit var bBranch: AppCompatTextView
    private lateinit var timeline: RecyclerView
    private val timelineAdapter: TimelineAdapter = TimelineAdapter()
    private var user: GoogleSignInAccount? = null
    private val dates = arrayListOf<Int>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (dates.size == 0) {
            for (i in 1..12) {
                dates.add(i)
            }
        }

        if (context != null) {
            user = GoogleSignIn.getLastSignedInAccount(requireContext())
        }

        view.apply {
            iAvatar = findViewById(R.id.fragment_settings_avatar)
            bName = findViewById(R.id.fragments_settings_bio_name)
            bEmail = findViewById(R.id.fragments_settings_bio_email)
            bSemester = findViewById(R.id.fragments_settings_bio_semester)
            bBranch = findViewById(R.id.fragments_settings_bio_branch)
            timeline = findViewById(R.id.fragment_settings_timeline)

            timeline.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            timeline.adapter = timelineAdapter
            timeline.scrollToPosition(Calendar.getInstance().get(Calendar.MONTH))

            if (user != null) {
                Picasso.get().load(user!!.photoUrl).into(iAvatar)
                bName.text = user!!.displayName
                bEmail.text = user!!.email
                bSemester.text = "Fixed data // SUMMER 2023"
                bBranch.text = "Fixed data // Fpoly HCM"
            }

            findViewById<AppCompatButton>(R.id.fragment_settings_logout_btn).let { btn ->
                btn.setOnFocusChangeListener { v, hasFocus ->
                    (v as AppCompatButton).setTextColor(
                        resources.getColor(
                            if (hasFocus) R.color.background else R.color.primary, null
                        )
                    )
                }
                btn.setOnClickListener { v ->
                    (v as AppCompatButton).setTextColor(
                        resources.getColor(
                            R.color.background, null
                        )
                    )

                    requireActivity().let { activity ->
                        GoogleSignIn.getClient(
                            activity,
                            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail().build()
                        ).signOut().continueWith {
                            startActivity(Intent(requireContext(), AuthActivity::class.java))
                            requireActivity().finish()
                        }
                    }
                }
            }

            // buttons
            findViewById<AppCompatButton>(R.id.fragment_settings_notes_btn).setOnClickListener {
                NotesFragment().show(childFragmentManager, null)
            }
        }
    }

    inner class TimelineAdapter : RecyclerView.Adapter<TimelineAdapter.ViewHolder>() {
        inner class ViewHolder(view: View, viewType: Int) : RecyclerView.ViewHolder(view) {
            var timelineView: TimelineView

            init {
                timelineView = itemView.findViewById(R.id.timeline)
                timelineView.initLine(viewType)
            }

            val text =
                view.findViewById<AppCompatTextView>(R.id.fragment_settings_timeline_item_text)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.settings_fragment_timeline_item_even, parent, false)
            return ViewHolder(view, viewType)
        }

        override fun getItemCount(): Int = dates.size

        override fun getItemViewType(position: Int): Int {
            return TimelineView.getTimeLineViewType(position, itemCount)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val i = dates[position]

            if (Calendar.getInstance().get(Calendar.MONTH) >= i) {
                holder.text.background = ResourcesCompat.getDrawable(
                    resources, R.drawable.badge_timeline_date_reversed_dark, null
                )
                holder.text.setTextColor(resources.getColor(R.color.primary))
                resources.getColor(R.color.background).apply {
                    holder.timelineView.setMarkerColor(this)
                    holder.timelineView.setEndLineColor(this, holder.itemViewType)
                    holder.timelineView.setStartLineColor(this, holder.itemViewType)
                }
            } else {
                holder.text.background = ResourcesCompat.getDrawable(
                    resources, R.drawable.badge_timeline_date_reversed, null
                )
                holder.text.setTextColor(resources.getColor(R.color.background))
                resources.getColor(R.color.primary).apply {
                    holder.timelineView.setMarkerColor(this)
                    holder.timelineView.setEndLineColor(this, holder.itemViewType)
                    holder.timelineView.setStartLineColor(this, holder.itemViewType)
                }
            }

            holder.text.text = "Tháng ${i}"
        }
    }
}