package com.ibashkimi.wheel.firestore.core

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.ibashkimi.wheel.core.User
import com.ibashkimi.wheel.core.model.core.Connection
import com.ibashkimi.wheel.core.model.core.Content
import com.ibashkimi.wheel.core.model.core.Event
import com.ibashkimi.wheel.core.model.posts.Like

fun QuerySnapshot.toUserList(): List<User> {
    return this.map { it.toUser() }
}

fun DocumentSnapshot.toUser() = User(
    uid = id,
    displayName = getString("displayName"),
    email = getString("email"),
    nickname = getString("nickname"),
    imageUrl = getString("imageUrl"),
    info = getString("info"),
    followerCount = getLong("follower")?.toInt() ?: 0,
    followedCount = getLong("followed")?.toInt() ?: 0,
    createdAt = getDate("created")?.time ?: 0L,
    lastLogin = getDate("lastLogin")?.time ?: 0L
)

fun DocumentSnapshot.toEvent(): Event {
    val type = getString("type")
    val uid = id
    val userId = getString("userId")!!
    val created = getDate("created")!!
    val done = getBoolean("done")!!
    return when (type) {
        "NewFollowRequestEvent" -> Event.NewFollowRequestEvent(
            uid, userId, created, type, done, getString("fromUser")!!
        )
        "FollowAcceptedEvent" -> Event.FollowAcceptedEvent(
            uid, userId, created, type, done, getString("fromUser")!!
        )
        else -> throw IllegalArgumentException("Unknown event type $type.")
    }
}

fun Event.toMap(): Map<String, Any> = when (this) {
    is Event.NewFollowRequestEvent -> mapOf<String, Any>(
        "userId" to userId,
        "created" to created,
        "done" to done,
        "type" to "NewFollowRequestEvent",
        "fromUser" to fromUserId
    )
    is Event.FollowAcceptedEvent -> mapOf<String, Any>(
        "userId" to userId,
        "created" to created,
        "done" to done,
        "type" to "FollowAcceptedEvent",
        "fromUser" to fromUserId
    )
}

fun DocumentSnapshot.toConnection(): Connection? {
    val fromUser = getString("fromUser")
    val created = getDate("created")
    val state = getString("state")
    val toUser = getString("toUser")

    if (fromUser == null || created == null || state == null || toUser == null)
        return null

    return Connection(
        uid = id,
        fromUserId = fromUser,
        created = created,
        state = state,
        toUserId = toUser,
        type = getString("type")
    )
}

fun QuerySnapshot.toConnectionList(): List<Connection> = mapNotNull { it.toConnection() }

fun Connection.toMap(): Map<String, Any?> = mapOf(
    "fromUser" to fromUserId,
    "created" to created,
    "state" to state,
    "toUser" to toUserId,
    "type" to type
)

fun QuerySnapshot.toEventList(): List<Event> = mapNotNull { it.toEvent() }

fun DocumentSnapshot.toLike(): Like? {
    val userId = getString("userId")
    val contentId = getString("contentId")
    val created = getLong("created")
    return if (userId != null && contentId != null && created != null) {
        Like(
            userId = getString("userId")!!,
            contentId = getString("contentId")!!,
            created = getLong("created")!!
        )
    } else null
}

fun DocumentSnapshot.toContent(type: String): Content = when (type) {
    "text" -> Content.Text(getString("contentText") ?: "")
    "image" -> Content.Media.Image(getString("contentUri") ?: "", getString("contentText") ?: "")
    "video" -> Content.Media.Video(getString("contentUri") ?: "", getString("contentText") ?: "")
    "animation" -> Content.Media.Animation(
        getString("contentUri") ?: "",
        getString("contentText") ?: ""
    )
    else -> Content.Unsupported(type)
}

fun Content.toType(): String = when (this) {
    is Content.Text -> "text"
    is Content.Media.Image -> "image"
    is Content.Media.Video -> "video"
    is Content.Media.Animation -> "animation"
    is Content.Unsupported -> "unsupported"
}
