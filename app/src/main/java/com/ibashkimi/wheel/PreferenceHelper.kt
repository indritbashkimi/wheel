package com.ibashkimi.wheel

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class PreferenceHelper(
    private val context: Context,
    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
) {

    var nightMode: String
        get() = sharedPreferences.getString("night_mode", null) ?: "system_default"
        set(value) {
            sharedPreferences.edit().putString("night_mode", value).apply()
        }

    var mapStyle: String
        get() = sharedPreferences.getString("map_style", null) ?: "normal"
        set(value) {
            sharedPreferences.edit().putString("map_style", value).apply()
        }
}
