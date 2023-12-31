package io.acay.fpl.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import io.acay.fpl.R
import io.acay.fpl.hooks.ViewAnimation.Instance.customOnCLick
import io.acay.fpl.service.SessionRenewService
import okhttp3.FormBody
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject

class AuthActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private val RC_SIGN_IN = 1337

    private val gSignInOptions =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
    private lateinit var gSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.auth_activity)

        val swapLoader: AppCompatImageView = findViewById(R.id.auth_swap_section_loader)
        val swapAuth: LinearLayoutCompat = findViewById(R.id.auth_swap_section_auth)

        Glide.with(this)
            .load("https://media.discordapp.net/attachments/1062430575438344264/1139073814349758574/7GgMHwj.gif")
            .into(swapLoader)

        // set sign in client
        gSignInClient = GoogleSignIn.getClient(this, gSignInOptions)

        // check if user is previously logged in
        GoogleSignIn.getLastSignedInAccount(this)?.let {
            return signInUser(it)
        }

        // display
        swapLoader.visibility = View.GONE
        swapAuth.visibility = View.VISIBLE

        // proceed with the rest of the activity
        getRandomQuote()

        // sign in button
        findViewById<AppCompatButton>(R.id.auth_activity_signInBtn).setOnClickListener {
            (it as AppCompatButton).customOnCLick()
            startActivityForResult(gSignInClient.signInIntent, RC_SIGN_IN)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                signInUser(task.getResult(ApiException::class.java))
            } catch (e: ApiException) {
                Toast.makeText(this, "Failed to sign in", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInUser(account: GoogleSignInAccount) {
        if (account.email!!.endsWith("@fpt.edu.vn", true)) {
            initSession(account) { t ->
                t ?: return@initSession Toast.makeText(this, "Failed to log in", Toast.LENGTH_LONG)
                    .show()

                with(Intent(this, SessionRenewService::class.java)) {
                    stopService(this)
                    startService(this)
                }

                getSharedPreferences("fpl_u", Context.MODE_PRIVATE).edit().putString("t", t).apply()
                startActivity(Intent(this, MainActivity::class.java).setAction(null))
                finish()
            }
        } else {
            gSignInClient.signOut()
            getSharedPreferences("fpl_u", Context.MODE_PRIVATE).edit().remove("t").apply()

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

            try {
                OkHttpClient.Builder().build().newCall(req).execute().use {
                    val jsonObj = JSONArray(it.body!!.string())
                    (jsonObj.get(0) as JSONObject).let { obj ->
                        Handler(mainLooper).post {
                            findViewById<AppCompatTextView>(R.id.auth_activity_quote).text =
                                "${obj.getString("content")}\n\n- ${obj.getString("author")}"
                        }
                    }
                }
            } catch (e: Exception) {
                Handler(mainLooper).post {
                    findViewById<AppCompatTextView>(R.id.auth_activity_quote).text =
                        "Uh oh, this wasn't suppose to happen"
                }
            }
        }.start()
    }

    private fun initSession(u: GoogleSignInAccount, t: (String?) -> Unit) {
        Thread {
            val req =
                okhttp3.Request.Builder().url("http://acay.atwebpages.com/asm/api/initSession.php")
                    .post(FormBody.Builder().add("u", u.email!!).build()).build()

            try {
                OkHttpClient.Builder().build().newCall(req).execute().use {
                    val body = it.body!!.string()
                    val jsonObj = JSONObject(body)
                    Handler(mainLooper).post { t.invoke(jsonObj.getString("t")) }
                }
            } catch (e: Exception) {
                Log.e("e", e.message!!)
                Handler(mainLooper).post { t.invoke(null) }
            }
        }.start()
    }
}