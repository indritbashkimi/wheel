package com.ibashkimi.wheel.firebase.model

import com.google.firebase.database.Exclude
import com.ibashkimi.wheel.firebase.data.mapToDomain

data class FirebaseMessage(
    @Exclude override var uid: String,
    var userId: String?,
    var chatId: String?,
    var createdAt: Long,
    var text: String?
) : BaseFirebaseModel(uid) {

    constructor() : this("", null, null, 0, null)

    fun map() = this.mapToDomain()
}