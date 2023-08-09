package io.acay.fpl.fragments.notes

import android.app.AlertDialog
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.acay.fpl.R
import io.acay.fpl.model.LNote
import io.acay.fpl.service.sqlite.NoteStore
import io.noties.markwon.Markwon
import java.util.Locale

class NotesFragment : BottomSheetDialogFragment(R.layout.notes_list) {
    private lateinit var recycler: RecyclerView
    private lateinit var recyclerAdapter: NotesAdapter
    private lateinit var notes: ArrayList<LNote>
    private lateinit var store: NoteStore

    private var txtTitle: String = ""
    private var txtContent: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireContext().apply {
            store = NoteStore(this)
            notes = store.getNotes()
        }

        requireView().apply {
            recyclerAdapter = NotesAdapter()
            recycler = findViewById(R.id.fragment_notes_recycler)
            recycler.layoutManager = LinearLayoutManager(context)
            recycler.adapter = recyclerAdapter

            findViewById<AppCompatButton>(R.id.fragment_notes_add).setOnClickListener {
                val v = LayoutInflater.from(requireContext()).inflate(R.layout.notes_list_add, null)
                val d = AlertDialog.Builder(requireContext()).setView(v).create()

                val btnCancel: AppCompatButton = v.findViewById(R.id.note_dialog_btn_cancel)
                val btnCreate: AppCompatButton = v.findViewById(R.id.note_dialog_btn_create)

                val inputTitle: AppCompatEditText = v.findViewById(R.id.note_dialog_title)
                val inputContent: AppCompatEditText = v.findViewById(R.id.note_dialog_content)

                inputTitle.doOnTextChanged { text, _, _, _ -> txtTitle = text.toString() }
                inputContent.doOnTextChanged { text, _, _, _ -> txtContent = text.toString() }

                btnCancel.setOnClickListener { dismiss() }
                btnCreate.setOnClickListener {
                    if (txtContent.isNotEmpty() && txtTitle.isNotEmpty() && store.insertNote(
                            txtTitle, txtContent
                        )
                    ) {
                        d.dismiss()
                    } else {
                        Toast.makeText(context, "Failed to create note", Toast.LENGTH_SHORT).show()
                    }
                }

                d.show()
            }
        }
    }

    inner class NotesAdapter : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val nTitle: AppCompatTextView = view.findViewById(R.id.note_item_title)
            val nContent: AppCompatTextView = view.findViewById(R.id.note_item_content)
            val nTimestamp: AppCompatTextView = view.findViewById(R.id.note_item_timestamp)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.notes_list_item, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = notes.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val n = notes[position]

            holder.nTitle.text = n.title
            Markwon.create(requireContext()).setMarkdown(holder.nContent, n.content)
            holder.nTimestamp.text =
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(n.timestamp)
                    ?.let {
                        DateUtils.getRelativeTimeSpanString(it.time)
                    }
        }
    }
}