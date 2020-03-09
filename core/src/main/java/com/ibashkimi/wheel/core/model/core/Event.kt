package com.ibashkimi.wheel.core.model.core

import com.ibashkimi.wheel.core.User
import java.util.*

sealed class Event(
    val uid: String,
    val userId: String,
    val created: Date,
    val type: String,
    var done: Boolean
) {
    class NewFollowRequestEvent(
        uid: String,
        userId: String,
        created: Date,
        type: String,
        done: Boolean,
        val fromUserId: String,
        var fromUser: User? = null,
        var connection: Connection? = null
    ) : Event(uid, userId, created, type, done)

    class FollowAcceptedEvent(
        uid: String,
        userId: String,
        created: Date,
        type: String,
        done: Boolean,
        val fromUserId: String,
        var fromUser: User? = null,
        var connection: Connection? = null
    ) : Event(uid, userId, created, type, done)
}

