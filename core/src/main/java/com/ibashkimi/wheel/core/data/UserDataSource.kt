package com.ibashkimi.wheel.core.data

import androidx.paging.DataSource
import com.ibashkimi.wheel.core.Direction
import com.ibashkimi.wheel.core.User
import com.ibashkimi.wheel.core.model.core.Connection
import com.ibashkimi.wheel.core.model.core.Event
import kotlinx.coroutines.flow.Flow

interface UserDataSource {
    // User

    val currentUserId: String?

    fun createUser(): Flow<String>

    fun getUser(userId: String): Flow<User?>

    fun getAllUsers(
        limit: Long,
        direction: Direction,
        continuation: Any?
    ): Flow<List<User>>

    fun getAllUsersPaged(): DataSource.Factory<Any, User>

    // Connections

    fun insertConnection(connection: Connection): Flow<String>

    fun getConnection(fromUserId: String, toUserId: String): Flow<Connection?>

    fun deleteConnection(connection: Connection): Flow<Unit>

    fun updateConnection(connection: Connection): Flow<String>

    fun getConnections(
        userId: String,
        limit: Long,
        direction: Direction,
        continuation: Any?
    ): Flow<List<Connection>>

    fun getConnectionsPaged(): DataSource.Factory<Any, Connection>

    // Events

    fun insertEvent(event: Event): Flow<String>

    fun getEvent(eventId: String): Flow<Event?>

    fun deleteEvent(event: Event): Flow<Unit>

    fun getEvents(
        userId: String,
        limit: Long,
        direction: Direction,
        continuation: Any?
    ): Flow<List<Event>>

    fun getEventsPaged(userId: String): DataSource.Factory<Any, Event>

    fun updateEvent(event: Event): Flow<String>
}