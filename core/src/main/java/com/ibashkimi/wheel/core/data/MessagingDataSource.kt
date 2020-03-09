package com.ibashkimi.wheel.core.data

import androidx.paging.DataSource
import com.ibashkimi.wheel.core.Direction
import com.ibashkimi.wheel.core.model.messaging.Message
import com.ibashkimi.wheel.core.model.messaging.Room
import kotlinx.coroutines.flow.Flow
import java.util.*

interface MessagingDataSource {
    // Chat

    fun createChat(chat: Room): Flow<String>

    fun deleteChat(chatId: String): Flow<Unit>

    fun clearChat(chatId: String): Flow<Unit>

    fun getChat(chatId: String): Flow<Room?>

    fun getChatListPaged(userId: String): DataSource.Factory<Any, Room>

    fun getChatList(
        userId: String,
        start: Date,
        limit: Long,
        direction: Direction
    ): Flow<List<Room>>

    // Messages

    fun createMessage(chatId: String, message: String): Flow<String>

    fun deleteMessage(chatId: String, messageId: String): Flow<Unit>

    fun getMessages(
        chatId: String,
        limit: Long,
        startAt: Date,
        direction: Direction
    ): Flow<List<Message>>

    fun getMessagesPaged(chatId: String): DataSource.Factory<Any, Message>

    fun getMessage(chatId: String, messageId: String): Flow<Message?>
}
