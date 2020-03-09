package com.ibashkimi.wheel.firebase.model

import com.google.firebase.database.Exclude

data class FirebaseChat(
    @Exclude override var uid: String,
    var imageUrl: String?,
    var name: String?,
    var createdAt: Long,
    var creator: String,
    @Exclude var fromUser: FirebaseUser?,
    @Exclude var toUser: FirebaseUser?,
    @Exclude var users: List<FirebaseUser>?,
    @Exclude var usersIds: List<String>?
) : BaseFirebaseModel(uid) {
    constructor() : this("", null, null, 0, "", null, null, null, null)
}
