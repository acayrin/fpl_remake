package io.acay.fpl.service.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import io.acay.fpl.model.LNote

class NoteStore(context: Context) : SQLiteOpenHelper(context, "NoteStore", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.apply {
            execSQL(
                "create table if not exists notes(" + "id integer primary key autoincrement," + "title varchar not null," + "content varchar not null," + "timestamp datetime default current_timestamp)"
            )
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // nothing to do
    }

    fun insertNote(title: String, content: String): Boolean {
        val values = ContentValues()
        values.put("title", title)
        values.put("content", content)

        return writableDatabase.insert("notes", null, values) != -1L
    }

    fun deleteNote(id: Int): Boolean = writableDatabase.delete("notes", "id = \"${id}\"", null) > 0

    fun insertNote(value: LNote): Boolean = insertNote(value.title, value.content)

    fun getNotes(): ArrayList<LNote> {
        val out = arrayListOf<LNote>()

        readableDatabase.apply {
            val cur = rawQuery("select * from notes order by timestamp desc", null)
            if (cur.moveToFirst()) {
                do {
                    out.add(
                        LNote(
                            id = cur.getInt(0),
                            title = cur.getString(1),
                            content = cur.getString(2),
                            timestamp = cur.getString(3),
                        )
                    )
                } while (cur.moveToNext())
            }
            cur.close()
        }

        return out
    }
}