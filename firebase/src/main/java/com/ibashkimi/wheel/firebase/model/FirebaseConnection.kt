package com.ibashkimi.wheel.firebase.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.ibashkimi.wheel.core.model.core.Connection
import java.util.*

@IgnoreExtraProperties
data class FirebaseConnection(
    @Exclude override var uid: String,
    var fromUserId: String?,
    var createdAt: Long,
    var state: String?,  // confirmed, pending etc.
    var toUserId: String?,
    var type: String?
) : BaseFirebaseModel(uid) {

    constructor() : this("", null, 0, null, null, null)

    @get:Exclude
    val isConfirmed: Boolean
        get() = "confirmed" == state

    @get:Exclude
    val isPending: Boolean
        get() = "pending" == state

    @Exclude
    fun toMap(): Map<String, Any?> {
        val result = HashMap<String, Any?>()
        result["uid"] = uid
        result["fromUserId"] = fromUserId
        result["createdAt"] = createdAt
        result["state"] = state
        result["toUserId"] = toUserId
        result["type"] = type
        return result
    }

    fun map() = Connection(
        uid,
        fromUserId!!,
        createdAt,
        state,
        toUserId!!,
        type
    )
}