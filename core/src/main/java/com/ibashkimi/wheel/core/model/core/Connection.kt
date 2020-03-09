package com.ibashkimi.wheel.core.model.core

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Connection(
    val uid: String?,
    val fromUserId: String,
    val created: Date,
    val state: String? = null, // confirmed, pending etc.
    val toUserId: String,
    val type: String? = null
) : Parcelable {

    fun isConfirmed(): Boolean {
        return "confirmed" == state
    }

    fun isPending(): Boolean {
        return "pending" == state
    }
}
