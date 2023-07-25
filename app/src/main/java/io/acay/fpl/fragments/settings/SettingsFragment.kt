package io.acay.fpl.fragments.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import io.acay.fpl.R
import io.acay.fpl.activity.AuthActivity

class SettingsFragment : Fragment(R.layout.settings_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<AppCompatButton>(R.id.fragment_settings_logout_btn).let { btn ->
            btn.setOnFocusChangeListener { v, hasFocus ->
                (v as AppCompatButton).setTextColor(
                    resources.getColor(
                        if (hasFocus) R.color.background else R.color.primary, null
                    )
                )
            }
            btn.setOnClickListener { v ->
                (v as AppCompatButton).setTextColor(resources.getColor(R.color.background, null))

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
    }
}