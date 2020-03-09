package com.ibashkimi.wheel.core.model.posts

import android.os.Parcelable
import com.ibashkimi.wheel.core.User
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Comment(
    val uid: String,
    val content: Content,
    val createdAt: Long,
    val postId: String,
    val userId: String,
    var user: User? = null
) : Parcelable