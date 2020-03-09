package com.ibashkimi.wheel.core.model.posts

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Post(
    val uid: String,
    val userId: String,
    val position: Position?,
    val created: Long,
    val content: Content,
    var liked: Boolean = false
) : Parcelable