package com.ibashkimi.wheel.firebase.model

import android.os.Parcelable
import com.ibashkimi.wheel.core.User
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Chat(
    val uid: String,
    val imageUrl: String? = null,
    val name: String = "",
    val createdAt: Long = 0,
    val creator: String? = null,
    val fromUser: User? = null,
    val toUser: User? = null,
    val usersIds: List<String>? = null
) : Parcelable