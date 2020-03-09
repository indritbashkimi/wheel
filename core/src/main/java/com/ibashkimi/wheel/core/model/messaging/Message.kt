package com.ibashkimi.wheel.core.model.messaging

import com.ibashkimi.wheel.core.User
import java.util.*

data class Message(
    val id: String,
    val chatId: String,
    val content: String,
    val userId: String,
    val created: Date,
    var user: User? = null
)
