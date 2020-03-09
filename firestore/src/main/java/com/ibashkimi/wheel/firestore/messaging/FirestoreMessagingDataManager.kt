package com.ibashkimi.wheel.firestore.messaging

import androidx.paging.DataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.ibashkimi.wheel.core.CachedData
import com.ibashkimi.wheel.core.Direction
import com.ibashkimi.wheel.core.data.MessagingDataSource
import com.ibashkimi.wheel.core.model.messaging.Message
import com.ibashkimi.wheel.core.model.messaging.Room
import com.ibashkimi.wheel.firestore.*
import com.ibashkimi.wheel.firestore.core.FirestoreUserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.*

class FirestoreMessagingDataManager : BaseFirestoreManager(), MessagingDataSource {

    val currentUserId: String? get() = currentUser?.uid

    override fun createChat(chat: Room): Flow<String> =
        writeNullFlow(
            db.collection("chats").document(chat.uid)
        ) {
            hashMapOf(
                "created" to chat.createdAt,
                "creator" to chat.creator,
                "imageUrl" to chat.imageUrl,
                "name" to chat.name,
                "updated" to chat.lastUpdated,
                "participants" to chat.participants
            )
        }

    override fun createMessage(chatId: String, message: String): Flow<String> =
        db.collection("chats").document(chatId)
            // todo update chat
            .collection("messages").document().writeFlow(
                hashMapOf(
                    "content" to message,
                    "user" to FirebaseAuth.getInstance().currentUser!!.uid,
                    "created" to Date()
                )
            )

    override fun getChat(chatId: String): Flow<Room?> =
        db.collection("chats").document(chatId).asFlow().map {
            if (it.exists()) it.toRoom() else null
        }

    override fun getChatList(
        userId: String,
        start: Date,
        limit: Long,
        direction: Direction
    ): Flow<List<Room>> =
        db.collection("chats")
            .whereArrayContains("participants", userId)
            .whereLessThan("updated", start)
            .orderBy(
                "updated",
                if (direction == Direction.DESCENDING) Query.Direction.DESCENDING else Query.Direction.ASCENDING
            )
            .limit(limit)
            .asFlow()
            .map { it.toRoomList() }


    override fun getChatListPaged(userId: String): DataSource.Factory<Any, Room> {
        val query = db.collection("chats")
            .whereArrayContains("participants", userId)
            .orderBy("updated", Query.Direction.DESCENDING)
        return FirestoreDataSourceFactory(
            query
        ) {
            it.map { query ->
                val res = query.toRoom()
                val cachedUser = CachedData {
                    FirestoreUserManager().getUser(it)
                }
                res.userParticipants = res.participants.map {
                    cachedUser.getItem(it)!!
                }
                res
            }
        } as DataSource.Factory<Any, Room>
    }

    override fun deleteChat(chatId: String): Flow<Unit> =
        clearChat(chatId)
            .map { db.collection("chats").document(chatId).delete() }
            .map { Unit }
            .flowOn(Dispatchers.IO)

    override fun clearChat(chatId: String): Flow<Unit> = flow {
        val messagesRef = db.collection("chats").document(chatId)
            .collection("messages")
        messagesRef.deleteCollection(1000)
        messagesRef.document().delete()
        emit(Unit)
    }

    override fun deleteMessage(chatId: String, messageId: String): Flow<Unit> =
        db.collection("chats").document(chatId)
            .collection("messages").document(messageId)
            .deleteAsFlow()

    override fun getMessages(
        chatId: String,
        limit: Long,
        startAt: Date,
        direction: Direction
    ): Flow<List<Message>> =
        db.collection("chats").document(chatId)
            .collection("messages")
            .startAt(startAt)
            .orderBy("created", direction.toFirestoreDirection())
            .limit(limit)
            .asFlow()
            .map { it.toMessageList(chatId) }

    override fun getMessagesPaged(chatId: String): DataSource.Factory<Any, Message> {
        val query = db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("created", Query.Direction.DESCENDING)
        val cachedUser = CachedData {
            FirestoreUserManager().getUser(it)
        }
        return FirestoreDataSourceFactory(
            query
        ) {
            it.map { query ->
                val message = query.toMessage(chatId)
                message.user = cachedUser.getItem(message.userId)!!
                message
            }
        } as DataSource.Factory<Any, Message>
    }

    override fun getMessage(chatId: String, messageId: String): Flow<Message> =
        db.collection("messages").document(messageId)
            .asFlow()
            .map { it.toMessage(chatId) }

    fun getLastMessage(chatId: String): Flow<Message?> =
        db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("created", Query.Direction.DESCENDING)
            .limit(1)
            .asFlow()
            .flowOn(Dispatchers.IO)
            .map { it.toMessageList(chatId).lastOrNull() }
}