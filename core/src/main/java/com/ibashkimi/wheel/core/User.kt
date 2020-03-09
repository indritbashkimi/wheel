package com.ibashkimi.wheel.core

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val uid: String,

    val displayName: String? = null,

    val email: String? = null,

    val nickname: String? = null,

    val imageUrl: String? = null,

    val info: String? = null,

    val followerCount: Int = 0,

    val followedCount: Int = 0,

    val createdAt: Long = 0,

    val lastLogin: Long = 0
) : Parcelable