package com.ibashkimi.wheel.firebase.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FirebaseComment(
    @Exclude override var uid: String,
    var content: Content?,
    var createdAt: Long,
    var postId: String?,
    var userId: String?,
    var username: String?,
    var nickname: String?
) : BaseFirebaseModel(uid) {
    constructor() : this("", null, 0, null, null, null, null)
}
