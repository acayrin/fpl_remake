package io.acay.fpl.fragments.classes.sub

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.acay.fpl.R
import io.acay.fpl.fragments.classes.sub.adapter.UpcomingClassesAdapter
import io.acay.fpl.model.ClassF
import java.text.SimpleDateFormat
import java.util.Random

class UpcomingClassesFragment : Fragment(R.layout.classes_fragment_sub_upcoming) {
    private val classList = arrayListOf<ClassF>()
    private lateinit var recyclerAdapter: UpcomingClassesAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        for (i in 0..20) {
            classList.add(
                ClassF(
                    i,
                    SimpleDateFormat().format(System.currentTimeMillis()),
                    "Room ${i}",
                    "Location ${i}",
                    "Subject ID ${i}",
                    "Subject ${i}",
                    "Class ${i}",
                    "Teacher #${i}",
                    Random().nextInt(5),
                    "xx:xx",
                    "yy:yy",
                    null,
                    null
                )
            )
        }

        view.findViewById<RecyclerView>(R.id.classes_fragment_sub_upcoming_recycler)
            .let { recyclerView ->
                recyclerAdapter = UpcomingClassesAdapter(classList)

                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = recyclerAdapter
            }
    }
}