package io.acay.fpl.hooks

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import io.acay.fpl.R

class ViewAnimation {

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

        @SuppressLint("ClickableViewAccessibility")
        fun AppCompatButton.customOnCLick(r: (() -> Unit)? = null) {
            try {
                setOnTouchListener { _, event ->
                    val bg: Drawable
                    val fg: Int

                    if (this.textColors.defaultColor == resources.getColor(R.color.primary, null)) {
                        bg = ResourcesCompat.getDrawable(
                            resources, R.drawable.badge_light, null
                        )!!
                        fg = R.color.background
                    } else {
                        bg = ResourcesCompat.getDrawable(
                            resources, R.drawable.badge_dark, null
                        )!!
                        fg = R.color.primary
                    }

                    background = bg
                    setTextColor(ResourcesCompat.getColor(resources, fg, null))

                    this.compoundDrawables.map { ico ->
                        ico?.setTint(ResourcesCompat.getColor(resources, fg, null))

                        return@map ico
                    }.let {
                        setCompoundDrawablesWithIntrinsicBounds(it[0], it[1], it[2], it[3])
                    }

                    if (event.action == MotionEvent.ACTION_DOWN) r?.invoke()

                    return@setOnTouchListener false
                }
            } catch (e: Exception) {
                // ignored
                Log.e("HERE", e.message.toString())
            }
        }
    }
}