package com.ibashkimi.wheel

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            PreferenceHelper(this).apply {
                // Apply theme before onCreate
                applyNightMode(nightMode)
            }
        }

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
    }

    override fun onSupportNavigateUp() = findNavController(R.id.main_nav_host_fragment).navigateUp()
}

fun applyGlobalNightMode(nightMode: String) {
    AppCompatDelegate.setDefaultNightMode(
        when (nightMode) {
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            "system_default" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            "battery_saver" -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            else -> throw IllegalArgumentException("Invalid night mode $nightMode.")
        }
    )
}

fun Activity.applyNightMode(nightMode: String) {
    applyGlobalNightMode(nightMode)
    //recreate()
}

@Suppress("unused")
fun AppCompatActivity.applyLocalNightMode(nightMode: String) {
    delegate.localNightMode = when (nightMode) {
        "dark" -> AppCompatDelegate.MODE_NIGHT_YES
        "system_default" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        "battery_saver" -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        "light" -> AppCompatDelegate.MODE_NIGHT_NO
        else -> throw IllegalArgumentException("Invalid night mode $nightMode.")
    }
    delegate.applyDayNight()
}
