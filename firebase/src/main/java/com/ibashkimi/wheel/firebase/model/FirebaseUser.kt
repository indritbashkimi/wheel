package com.ibashkimi.wheel.firebase.model

import com.google.firebase.database.Exclude
import com.ibashkimi.wheel.core.User

data class FirebaseUser(
    @Exclude override var uid: String,
    var displayName: String?,
    var email: String?,
    var nickname: String?,
    var imageUrl: String?,
    var info: String?,
    var followerCount: Int,
    var followedCount: Int,
    var createdAt: Long,
    var lastLogin: Long
) : BaseFirebaseModel(uid) {

    constructor() : this("", null, null, null, null, null, 0, 0, 0, 0)

    fun map() = User(
        uid,
        displayName,
        email,
        nickname,
        imageUrl,
        info,
        followerCount,
        followedCount,
        createdAt,
        lastLogin
    )
}