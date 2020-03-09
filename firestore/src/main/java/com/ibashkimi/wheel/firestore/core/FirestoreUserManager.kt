package com.ibashkimi.wheel.firestore.core

import android.util.Log
import androidx.paging.DataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.ibashkimi.wheel.core.Direction
import com.ibashkimi.wheel.core.User
import com.ibashkimi.wheel.core.data.UserDataSource
import com.ibashkimi.wheel.core.model.core.Connection
import com.ibashkimi.wheel.core.model.core.Event
import com.ibashkimi.wheel.firestore.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.*


@ExperimentalCoroutinesApi
class FirestoreUserManager : BaseFirestoreManager(), UserDataSource {

    // Users

    override val currentUserId: String? get() = currentUser?.uid

    override fun createUser(): Flow<String> {
        val user = FirebaseAuth.getInstance().currentUser!!
        val now = Date()
        val map = hashMapOf(
            "created" to now,
            "displayName" to user.displayName,
            "email" to user.email,
            "imageUrl" to user.photoUrl.toString(),
            "phoneNumber" to user.phoneNumber,
            "lastLogin" to now,
            "privateAccount" to false
        )
        return db.collection("users").document(currentUserId!!).writeFlow(map)
    }

    override fun getUser(userId: String): Flow<User?> =
        db.collection("users").document(userId).asFlow().map { it.toUser() }

    //override fun getAllUsers(): Flow<List<User>> = db.collection("users").asFlow().map { it.toUserList() }

    override fun getAllUsers(
        limit: Long,
        direction: Direction,
        continuation: Any?
    ): Flow<List<User>> =
        db.collection("users")
            .orderBy("created", direction.toFirestoreDirection())
            .withContinuation(continuation)
            .limit(limit)
            .asFlow()
            .map { it.toUserList() }

    override fun getAllUsersPaged(): DataSource.Factory<Any, User> {
        val query = db.collection("users")
            .orderBy("created", Query.Direction.DESCENDING)
        return FirestoreDataSourceFactory(query) {
            it.map { query -> query.toUser() }
        } as DataSource.Factory<Any, User>
    }

    // Connections

    override fun getConnection(fromUserId: String, toUserId: String): Flow<Connection?> =
        (if (fromUserId < toUserId) "$fromUserId$toUserId" else "$toUserId$fromUserId").let {
            db.collection("connections").document(it)
        }.asFlow().map { it.toConnection() }

    override fun getConnections(
        userId: String,
        limit: Long,
        direction: Direction,
        continuation: Any?
    ): Flow<List<Connection>> = db.collection("connections")
        .orderBy("created", direction.toFirestoreDirection())
        .withContinuation(continuation)
        .limit(limit)
        .asFlow()
        .map { it.toConnectionList() }

    override fun getConnectionsPaged(): DataSource.Factory<Any, Connection> {
        val query = db.collection("connections")
            .orderBy("created", Query.Direction.DESCENDING)
        return FirestoreDataSourceFactory(query) {
            it.map { query -> query.toConnection() }
        } as DataSource.Factory<Any, Connection>
    }


    override fun insertConnection(connection: Connection): Flow<String> {
        val from = connection.fromUserId
        val to = connection.toUserId
        val connectionId = if (from < to) "$from$to" else "$to$from"
        return db.collection("connections").document(connectionId)
            .writeFlow(connection.toMap())
    }

    override fun deleteConnection(connection: Connection): Flow<Unit> =
        db.collection("connections").document(connection.uid!!).delete().asFlow()

    override fun updateConnection(connection: Connection): Flow<String> =
        db.collection("connections").document(connection.uid!!).writeFlow(connection.toMap())

    // Events

    override fun insertEvent(event: Event): Flow<String> =
        db.collection("events").document().writeFlow(event.toMap())

    override fun getEvent(eventId: String): Flow<Event?> =
        db.collection("events").document(eventId).asFlow()
            .map { it.toEvent() }
            .transformLatest { it.toFullEventFlow() }

    private fun Event.toFullEventFlow(): Flow<Event> {
        return when (val event = this) {
            is Event.NewFollowRequestEvent -> {
                getUser(event.fromUserId).combine(
                    getConnection(
                        currentUserId!!,
                        event.fromUserId
                    )
                ) { user, connection ->
                    event.fromUser = user
                    event.connection = connection
                    event
                }.take(1)
            }
            is Event.FollowAcceptedEvent -> {
                getUser(event.fromUserId).combine(
                    getConnection(
                        currentUserId!!,
                        event.fromUserId
                    )
                ) { user, connection ->
                    event.fromUser = user
                    event.connection = connection
                    event
                }.take(1)
            }
        }
    }

    private suspend fun Event.toFullEvent() {
        when (val event = this) {
            is Event.NewFollowRequestEvent -> {
                getUser(event.fromUserId).combine(
                    getConnection(
                        currentUserId!!,
                        event.fromUserId
                    )
                ) { user, connection ->
                    event.fromUser = user
                    event.connection = connection
                    event
                }
            }
            is Event.FollowAcceptedEvent -> {
                getUser(event.fromUserId).combine(
                    getConnection(
                        currentUserId!!,
                        event.fromUserId
                    )
                ) { user, connection ->
                    event.fromUser = user
                    event.connection = connection
                    event
                }
            }
        }.take(1).collect {
            Log.d("FirestoreUserManager", "Collected")
        }
        Log.d("FirestoreUserManager", "Exit")
    }

    override fun deleteEvent(event: Event): Flow<Unit> =
        db.collection("events").document(event.uid).delete().asFlow()

    override fun updateEvent(event: Event): Flow<String> =
        db.collection("events").document(event.uid).writeFlow(event.toMap())

    override fun getEvents(
        userId: String,
        limit: Long,
        direction: Direction,
        continuation: Any?
    ): Flow<List<Event>> = db.collection("events")
        .whereEqualTo("userId", userId)
        .orderBy("created", direction.toFirestoreDirection())
        .withContinuation(continuation)
        .limit(limit)
        .asFlow()
        .map { snapshot ->
            val res = snapshot.toEventList()
            res.map { it.toFullEventFlow().collect { } }
            res
        }


    override fun getEventsPaged(userId: String): DataSource.Factory<Any, Event> {
        val query = db.collection("events")
            .whereEqualTo("userId", userId)
            .orderBy("created", Query.Direction.DESCENDING)
        return FirestoreDataSourceFactory(query) {
            it.map { query ->
                val res = query.toEvent()
                res.toFullEvent()
                res
            }
        } as DataSource.Factory<Any, Event>
    }
}