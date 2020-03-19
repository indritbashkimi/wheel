package com.ibashkimi.wheel.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.ibashkimi.wheel.R

class PrefsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)
    }
}