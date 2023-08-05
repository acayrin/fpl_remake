package io.acay.fpl.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import io.acay.fpl.R
import io.acay.fpl.hooks.ViewStartAnimation
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject

class AuthActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private val RC_SIGN_IN = 1337
    private val facilities = arrayListOf(
        "Lựa chọn cơ sở",
        "CĐ Hà Nội",
        "CĐ Hồ Chí Minh",
        "CĐ Cần thơ",
        "CĐ Đà Nẵng",
        "CĐ Tây Nguyên",
        "CĐ Hải Phòng",
        "CĐ Hà Nam",
        "CĐ Thanh Hóa",
        "CĐ Quy Nhơn",
        "CĐ Thái Nguyên",
        "CĐ Đồng Nai",
        "CĐ Hitech",
    )

    private val gSignInOptions =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
    private lateinit var gSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.auth_activity)
        ViewStartAnimation.Instance.hookView(this)

        // set sign in client
        gSignInClient = GoogleSignIn.getClient(this, gSignInOptions)

        // check if user is previously logged in
        GoogleSignIn.getLastSignedInAccount(this).let {
            if (it != null) signInUser(it)
        }

        // proceed with the rest of the activity
        getRandomQuote()

        // facility selector
        val facSelector = findViewById<AppCompatSpinner>(R.id.auth_activity_selector)
        ArrayAdapter(
            this, R.layout.auth_spinner_facilities, facilities
        ).let {
            it.setDropDownViewResource(R.layout.auth_spinner_facilities)

            facSelector.adapter = it
            facSelector.onItemSelectedListener = this@AuthActivity

            val selectedFac = getSharedPreferences("prefs", MODE_PRIVATE).getInt("fac", -1)
            if (selectedFac > -1) {
                facSelector.setSelection(selectedFac)
            }
        }

        // sign in button
        findViewById<AppCompatButton>(R.id.auth_activity_signInBtn).let {
            it.setOnTouchListener { v, event ->
                var bg = R.drawable.badge_dark
                var fg = R.color.primary

                if (event.action == MotionEvent.ACTION_DOWN) {
                    startActivityForResult(gSignInClient.signInIntent, RC_SIGN_IN)

                    bg = R.drawable.badge_light
                    fg = R.color.background
                }

                v.background = ResourcesCompat.getDrawable(resources, bg, null)
                it.setTextColor(ResourcesCompat.getColor(resources, fg, null))

                val ico = ResourcesCompat.getDrawable(resources, R.drawable.vec_google_logo, null)
                ico!!.setTint(ResourcesCompat.getColor(resources, fg, null))
                it.setCompoundDrawablesWithIntrinsicBounds(ico, null, null, null)

                return@setOnTouchListener v.performClick()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                signInUser(task.getResult(ApiException::class.java))
            } catch (e: ApiException) {
                Toast.makeText(this, "Failed to sign in", Toast.LENGTH_SHORT).show()
                throw(e)
            }
        }
    }

    private fun signInUser(account: GoogleSignInAccount) {
        if (account.email!!.endsWith("@fpt.edu.vn", true)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            gSignInClient.signOut()

            Toast.makeText(this, "Not a FPT account", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        getSharedPreferences("prefs", MODE_PRIVATE).edit().putInt("fac", position).apply()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // don't do anything
    }

    private fun getRandomQuote() {
        Thread {
            val req = okhttp3.Request.Builder()
                .url("https://api.quotable.io/quotes/random?tags=education").get().build()

            OkHttpClient.Builder().build().newCall(req).execute().use {
                val jsonObj = JSONArray(it.body!!.string())
                (jsonObj.get(0) as JSONObject).let { obj ->
                    Handler(mainLooper).post {
                        findViewById<AppCompatTextView>(R.id.auth_activity_quote).text =
                            "${obj.getString("content")}\n\n- ${obj.getString("author")}"
                    }
                }
            }
        }.start()
    }
}