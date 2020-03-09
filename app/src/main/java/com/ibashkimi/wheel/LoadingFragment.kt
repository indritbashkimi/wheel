package com.ibashkimi.wheel

import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

class LoadingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.splash_screen, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val launchTime = SystemClock.uptimeMillis()
        val delayMillis: Long = 0 //300 splash time
        ViewModelProvider(this).get(AuthViewModel::class.java).userLiveData
            .observe(viewLifecycleOwner, Observer { userId ->
                val now = SystemClock.uptimeMillis()
                val delay: Long =
                    if (now - launchTime >= delayMillis) delayMillis
                    else delayMillis - now + launchTime
                Handler().postDelayed({
                    findNavController().navigate(
                        if (userId == null) {
                            LoadingFragmentDirections.actionLoadingToSignIn()
                        } else {
                            LoadingFragmentDirections.actionLoadingToHome()
                        }
                    )
                }, delay)
            })
    }
}