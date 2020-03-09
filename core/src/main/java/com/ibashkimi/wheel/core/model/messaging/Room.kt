package com.ibashkimi.wheel.core.model.messaging

import com.ibashkimi.wheel.core.User
import java.util.*


data class Room(
    val uid: String,
    val imageUrl: String? = null,
    val name: String?,
    val createdAt: Date,
    val creator: String,
    val participants: List<String>,
    val lastUpdated: Date = Date(),
    val lastMessageId: String?,
    var userParticipants: List<User>? = null
)