package com.ibashkimi.wheel.firebase.data

import com.ibashkimi.wheel.core.User
import com.ibashkimi.wheel.core.model.core.Connection
import com.ibashkimi.wheel.core.model.messaging.Message
import com.ibashkimi.wheel.core.model.posts.Comment
import com.ibashkimi.wheel.core.model.posts.Position
import com.ibashkimi.wheel.core.model.posts.Post
import com.ibashkimi.wheel.firebase.model.*
import java.util.*


fun FirebaseConnection.mapToDomain(): Connection =
    Connection(
        uid,
        fromUserId!!,
        createdAt,
        state,
        toUserId!!,
        type
    )

fun List<FirebaseConnection>.mapToConnections() = map { it.mapToDomain() }

fun FirebaseEvent.mapToDomain(): Notification =
    Notification(
        uid,
        createdAt,
        type,
        objectUid,
        done,
        (data as FirebaseConnection).mapToDomain()
    )

fun List<FirebaseEvent>.mapToEvents() = map { it.mapToDomain() }

fun FirebaseUser.mapToDomain() = User(
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

fun List<FirebaseUser>.mapToUsers() = map { it.mapToDomain() }

fun Connection.mapToFirebase() = FirebaseConnection()
    .also {
        it.uid = uid!!
        it.fromUserId = fromUserId
        it.createdAt = created
        it.state = state
        it.toUserId = toUserId
        it.type = type
    }

fun Notification.mapToFirebase() = FirebaseEvent()
    .also {
        it.uid = uid
        it.createdAt = createdAt
        it.type = type
        it.objectUid = objectUid
        it.done = done
        it.data = (data as Connection).mapToFirebase()
    }

fun User.mapToFirebase() = FirebaseUser()
    .also {
        it.uid = uid
        it.displayName = displayName
        it.email = email
        it.nickname = nickname
        it.imageUrl = imageUrl
        it.info = info
        it.followerCount = followerCount
        it.followedCount = followedCount
        it.createdAt = createdAt
        it.lastLogin = lastLogin
    }

fun FirebaseChat.mapToDomain(): Chat =
    Chat(
        uid,
        imageUrl,
        name!!,
        createdAt,
        creator,
        fromUser?.map(),
        toUser?.map(),
        usersIds
    )

fun List<FirebaseChat>.mapToChats() = map { it.mapToDomain() }

fun FirebaseMessage.mapToDomain(): Message =
    Message(
        uid,
        userId!!,
        chatId!!,
        text ?: "",
        Date(createdAt)
    )

fun List<FirebaseMessage>.mapToMessages() = map { it.mapToDomain() }

fun Chat.mapToFirebase() = FirebaseChat()
    .also {
        it.uid = uid
        it.imageUrl = imageUrl
        it.name = name
        it.createdAt = createdAt
        it.creator = creator!!
    }

fun Message.mapToFirebase() = FirebaseMessage()
    .also {
        it.uid = id
        it.userId = userId
        it.chatId = chatId
        it.createdAt = created.time
        it.text = content
    }

fun FirebaseComment.mapToDomain(): Comment =
    Comment(uid, content!!, createdAt, postId!!, userId!!, username!!, nickname!!)

fun List<FirebaseComment>.mapToComments(): List<Comment> = map { it.mapToDomain() }

fun FirebasePosition.mapToDomain() = Position(latitude, longitude)

fun FirebasePost.mapToDomain() = Post(uid, userId, position?.mapToDomain(), created, content!!)

fun List<FirebasePost>.mapToPosts() = map { it.mapToDomain() }

fun Comment.mapToFirebase() = FirebaseComment()
    .also {
        it.uid = uid
        it.content = content
        it.createdAt = createdAt
        it.postId = postId
        it.userId = userId
        it.username = username
        it.nickname = nickname
    }

fun Position.mapToFirebase() =
    FirebasePosition(
        latitude,
        longitude,
        address
    )

fun Post.mapToFirebase() = FirebasePost()
    .also {
        it.uid = uid
        it.userId = userId
        it.position = position?.mapToFirebase()
        it.created = created
        it.content = content
    }
