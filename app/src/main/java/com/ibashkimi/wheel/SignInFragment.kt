package com.ibashkimi.wheel

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.ibashkimi.wheel.core.toast
import com.ibashkimi.wheel.firebase.auth.getFirebaseAuthIntent
import com.ibashkimi.wheel.firestore.core.FirestoreUserManager
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class SignInFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_sign_in, container, false)
        root.findViewById<View>(R.id.sign_in_button).setOnClickListener { signIn() }
        return root
    }

    override fun onResume() {
        super.onResume()
        Handler().post {
            view?.findViewById<ImageView>(R.id.imageView)?.let {
                ObjectAnimator.ofFloat(it, "rotation", 0f, 360f).apply {
                    duration = 3000
                    interpolator = BounceInterpolator()
                    start()
                }
            }
        }
    }

    private fun signIn() {
        startActivityForResult(
            getFirebaseAuthIntent(),
            RC_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RC_SIGN_IN -> if (resultCode == Activity.RESULT_OK) {
                // Create on firestore
                lifecycleScope.launch {
                    FirestoreUserManager()
                        .createUser()
                        .catch { toast("Cannot create user. ${it.message}") }
                        .collect {
                            toast("Welcome " + FirebaseAuth.getInstance().currentUser!!.displayName + " :)")
                            findNavController().navigate(R.id.action_signIn_to_home)
                        }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                toast("User didn't log in.")
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {

        private const val RC_SIGN_IN = 0
    }
}
