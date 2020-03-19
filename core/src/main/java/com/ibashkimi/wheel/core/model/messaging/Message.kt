package com.ibashkimi.wheel.core.model.messaging

import com.ibashkimi.wheel.core.User
import com.ibashkimi.wheel.core.model.core.Content
import java.util.*

data class Message(
    val id: String,
    val chatId: String,
    val content: Content,
    val userId: String,
    val created: Date,
    var user: User? = null
)
