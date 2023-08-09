package io.acay.fpl.service.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import io.acay.fpl.model.sqlite.Notification

class NotificationStore(context: Context) :
    SQLiteOpenHelper(context, "NotificationStore", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.apply {
            execSQL(
                "create table if not exists notifications(" + "id integer primary key autoincrement," + "title varchar not null," + "description varchar not null," + "timestamp datetime default current_timestamp," + "seen bit not null)"
            )
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // nothing to do
    }

    fun insertNotification(title: String, description: String, seen: Boolean) {
        val values = ContentValues()
        values.put("title", title)
        values.put("description", description)
        values.put("seen", seen)

        writableDatabase.apply {
            insert("notifications", null, values)
        }
    }

    fun insertNotification(value: Notification) {
        insertNotification(value.title, value.description, value.seen)
    }

    fun setAsSeen(id: Int): Boolean {
        val values = ContentValues()
        values.put("seen", 1)

        return writableDatabase.update(
            "notifications", values, "id = ?", arrayOf(id.toString())
        ) > 0
    }

    fun setAllAsSeen(): Boolean {
        val values = ContentValues()
        values.put("seen", 1)

        return writableDatabase.update("notifications", values, null, null) > 0
    }

    fun getNotifications(unread: Boolean = false): ArrayList<Notification> {
        val out = arrayListOf<Notification>()

        readableDatabase.apply {
            val cur = rawQuery(
                "select * from notifications ${if (unread) "where seen = 0" else ""} order by timestamp desc",
                null
            )
            if (cur.moveToFirst()) {
                do {
                    out.add(
                        Notification(
                            id = cur.getInt(0),
                            title = cur.getString(1),
                            description = cur.getString(2),
                            timestamp = cur.getString(3),
                            seen = cur.getInt(4) == 1
                        )
                    )
                } while (cur.moveToNext())
            }
            cur.close()
        }

        return out
    }
}