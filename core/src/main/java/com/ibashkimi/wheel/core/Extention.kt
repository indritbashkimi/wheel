package com.ibashkimi.wheel.core

import android.content.Context
import android.text.format.DateUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.util.*


fun Long.relativeTimeSpan() = DateUtils.getRelativeTimeSpanString(this)!!

fun Date.relativeTimeSpan() = DateUtils.getRelativeTimeSpanString(time)!!

fun Context.toast(msg: String, duration: Int = Toast.LENGTH_LONG) =
    Toast.makeText(this, msg, duration).show()

fun Fragment.toast(msg: String, duration: Int = Toast.LENGTH_LONG) =
    Toast.makeText(requireContext(), msg, duration).show()

fun Fragment.toast(msg: Int, duration: Int = Toast.LENGTH_LONG) =
    Toast.makeText(requireContext(), getString(msg), duration).show()