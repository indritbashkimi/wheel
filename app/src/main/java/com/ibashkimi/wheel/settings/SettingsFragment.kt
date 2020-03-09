package com.ibashkimi.wheel.settings


import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.ibashkimi.wheel.PreferenceHelper
import com.ibashkimi.wheel.R
import com.ibashkimi.wheel.applyNightMode


class SettingsFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        val toolbar = root.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.title_settings)
        toolbar.setNavigationIcon(R.drawable.ic_back_nav)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        // Display the fragment as the main content.
        childFragmentManager.beginTransaction()
            .replace(R.id.container, PrefsFragment())
            .commit()

        return root
    }

    override fun onStart() {
        super.onStart()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        when (s) {
            "night_mode" -> {
                requireActivity().applyNightMode(
                    PreferenceHelper(requireContext(), sharedPreferences).nightMode
                )
            }
        }
    }
}
