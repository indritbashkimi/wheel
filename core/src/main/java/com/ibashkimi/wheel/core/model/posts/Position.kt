package com.ibashkimi.wheel.core.model.posts

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Position(val latitude: Double, val longitude: Double, val address: String? = null) :
    Parcelable
