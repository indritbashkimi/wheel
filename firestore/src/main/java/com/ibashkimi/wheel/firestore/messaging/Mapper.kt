package com.ibashkimi.wheel.firestore.messaging

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.ibashkimi.wheel.core.model.messaging.Message
import com.ibashkimi.wheel.core.model.messaging.Room

fun QuerySnapshot.toRoomList(): List<Room> = this.map { it.toRoom() }

fun DocumentSnapshot.toRoom() = Room(
    uid = id,
    imageUrl = getString("imageUrl"),
    name = getString("name"),
    createdAt = getDate("created")!!,
    creator = getString("creator")!!,
    participants = get("participants") as List<String>,
    lastUpdated = getDate("updated")!!,
    lastMessageId = getString("lastMessage")
)

fun QuerySnapshot.toMessageList(chatId: String): List<Message> = this.map { it.toMessage(chatId) }

fun DocumentSnapshot.toMessage(chatId: String) = Message(
    id = id,
    chatId = chatId,
    userId = getString("user")!!,
    created = getDate("created")!!,
    content = getString("content")!!
)

fun QueryDocumentSnapshot.toMessage(chatId: String) = Message(
    id = id,
    chatId = chatId,
    userId = getString("user")!!,
    created = getDate("created")!!,
    content = getString("content")!!
)
