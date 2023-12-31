package io.acay.fpl.fragments.home

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateUtils
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.acay.fpl.R
import io.acay.fpl.fragments.NotificationListFragment
import io.acay.fpl.fragments.news.ArticleDrawerFragment
import io.acay.fpl.hooks.ViewAnimation.Instance.customOnCLick
import io.acay.fpl.service.ClassListService
import io.acay.fpl.service.LatestNewsService
import io.acay.fpl.service.sqlite.NotificationStore
import io.noties.markwon.Markwon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale.US

class HomeFragment : Fragment(R.layout.overview_fragment_main) {
    private var user: GoogleSignInAccount? = null
    private lateinit var nTitle: AppCompatTextView
    private lateinit var nAuthor: AppCompatTextView
    private lateinit var nTimestamp: AppCompatTextView
    private lateinit var nContent: AppCompatTextView
    private lateinit var nReadMore: AppCompatButton

    private lateinit var cShift: AppCompatTextView
    private lateinit var cRoom: AppCompatTextView
    private lateinit var cSubject: AppCompatTextView
    private lateinit var cLocation: AppCompatTextView
    private lateinit var cSubjectId: AppCompatTextView
    private lateinit var cTeacher: AppCompatTextView
    private lateinit var cDuration: AppCompatTextView
    private lateinit var cOnlineLink: AppCompatTextView
    private lateinit var cDetails: AppCompatTextView
    private lateinit var cSelfNote: AppCompatEditText

    private lateinit var iAvatar: AppCompatImageView
    private lateinit var tCount: AppCompatTextView
    private lateinit var gText: AppCompatTextView
    private lateinit var t: String

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (context != null) {
            user = GoogleSignIn.getLastSignedInAccount(requireContext())
            t = requireContext().getSharedPreferences(
                "fpl_u", Context.MODE_PRIVATE
            ).getString("t", "")!!
        }

        // view binding
        view.apply {
            // set views
            tCount = findViewById(R.id.fragment_overview_notification_count)
            gText = findViewById(R.id.fragment_overview_greeting)
            iAvatar = findViewById(R.id.fragment_overview_avatar)

            nTitle = findViewById(R.id.fragment_overview_news_title)
            nAuthor = findViewById(R.id.fragment_overview_news_author)
            nTimestamp = findViewById(R.id.fragment_overview_news_timestamp)
            nContent = findViewById(R.id.fragment_overview_news_content)
            nReadMore = findViewById(R.id.fragment_overview_news_read_more)

            cShift = findViewById(R.id.fragment_overview_class_date)
            cRoom = findViewById(R.id.fragment_overview_class_room)
            cSubject = findViewById(R.id.fragment_overview_class_subject_name)
            cSubjectId = findViewById(R.id.fragment_overview_class_subject_id)
            cLocation = findViewById(R.id.fragment_overview_class_location)
            cTeacher = findViewById(R.id.fragment_overview_class_teacher)
            cDuration = findViewById(R.id.fragment_overview_class_duration)
            cOnlineLink = findViewById(R.id.fragment_overview_class_online_link)
            cDetails = findViewById(R.id.fragment_overview_class_details)
            cSelfNote = findViewById(R.id.fragment_overview_class_local_note)

            // data bindings
            Glide.with(context).load(user?.photoUrl).into(iAvatar)
            gText.text = "Greeting, ${user?.displayName ?: "Unknown"}"
            tCount.apply {
                fun getCount() = NotificationStore(context).getNotifications(true).size.toString()

                // base
                text = getCount()

                // auto check
                Thread {
                    while (true) {
                        Thread.sleep(10_000)
                        Handler(Looper.getMainLooper()) {
                            text = getCount()
                            return@Handler true
                        }
                    }
                }.start()

                // drawer
                setOnClickListener {
                    NotificationListFragment {
                        text = getCount()
                    }.show(childFragmentManager, null)
                }
            }
        }

        // Upcoming class
        ClassListService.getClasses(t, null, null) { list ->
            val index = list.indexOfFirst {
                SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", US
                ).parse(it.date)!!.time >= Date().time
            }
            if (index < 0) return@getClasses

            val latest = list[index]

            val relativeTime =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", US).parse(latest.date)?.let {
                    DateUtils.getRelativeTimeSpanString(it.time)
                }
            cShift.text = "Shift ${latest.shift} - ${relativeTime}"
            cRoom.text =
                latest.onlineLink?.takeIf { s -> s.isNotEmpty() && s != "null" } ?: latest.room
            cSubject.text = latest.subjectName
            cLocation.text = latest.location
            cSubjectId.text = latest.subjectId
            cTeacher.text = latest.teacher
            cDuration.text = "${latest.periodFrom}-${latest.periodTo}"
            cOnlineLink.text =
                latest.onlineLink?.takeIf { s -> s.isNotEmpty() && s != "null" } ?: ""
            cDetails.text = latest.details?.takeIf { s -> s.isNotEmpty() && s != "null" } ?: ""

            if (context != null) {
                val sp =
                    requireContext().getSharedPreferences("cn_${latest.id}", Context.MODE_PRIVATE)
                cSelfNote.apply {
                    if (context == null) return@apply

                    setText(sp.getString("content", null))

                    doOnTextChanged { text, _, _, _ ->
                        sp.edit().putString("content", text.toString()).apply()
                    }
                }
            }
        }

        // Latest news
        LatestNewsService.getArticles(t, null) {
            val latest = it[0]

            nTitle.text = latest.title
            nAuthor.text = latest.author
            nTimestamp.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", US).parse(latest.timestamp)
                ?.let { sdf -> DateUtils.getRelativeTimeSpanString(sdf.time) }

            with(latest.content.split(" ")) {
                var text = joinToString(" ")
                if (size > 30) {
                    text = "${subList(0, 30).joinToString(" ")}..."

                    nReadMore.visibility = View.VISIBLE
                } else {
                    nReadMore.visibility = View.GONE
                }

                if (context != null) {
                    val md = Markwon.create(requireContext())
                    md.setMarkdown(nContent, text)
                }
            }

            nReadMore.customOnCLick()
            nReadMore.setOnClickListener {
                ArticleDrawerFragment(latest).show(childFragmentManager, null)
            }
        }
    }
}