package com.ibashkimi.wheel.firebase.model

import com.google.firebase.database.Exclude

data class FirebaseEvent(
    @Exclude override var uid: String,
    var createdAt: Long,
    var type: String?,
    var objectUid: String?,
    var done: Boolean,
    @Exclude var data: Any?
) : BaseFirebaseModel(uid) {

    constructor() : this("", 0, null, null, false, null)

    //public boolean done;
    @Exclude
    fun toMap(): HashMap<String, Any?> {
        val result = HashMap<String, Any?>()
        result["uid"] = uid
        result["createdAt"] = createdAt
        result["type"] = type
        result["objectUid"] = objectUid
        result["done"] = done
        return result
    }

    fun map() = Notification(
        uid,
        createdAt,
        type,
        objectUid,
        done,
        data
    )
}