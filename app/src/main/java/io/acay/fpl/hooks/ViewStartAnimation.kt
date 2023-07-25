package io.acay.fpl.hooks

import android.graphics.drawable.AnimationDrawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import java.lang.Exception

class ViewStartAnimation {

    object Instance {
        private fun View.getAllChildren(): List<View> {
            val res = ArrayList<View>()
            if (this !is ViewGroup) {
                res.add(this)
            } else {
                for (index in 0 until this.childCount) {
                    val child = this.getChildAt(index)
                    res.addAll(child.getAllChildren())
                }
            }
            return res
        }

        fun hookView(a: AppCompatActivity) {
            for (v in (a.findViewById(android.R.id.content) as ViewGroup).getChildAt(0).getAllChildren()) {
                try {
                    (v.background as AnimationDrawable).start()
                } catch(e: Exception) {
                    // ignored
                    Log.e("HERE", e.message.toString())
                }
            }
        }
    }
}