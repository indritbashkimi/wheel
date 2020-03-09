package com.ibashkimi.wheel.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import com.ibashkimi.wheel.core.model.core.Connection
import com.ibashkimi.wheel.core.model.core.Event
import com.ibashkimi.wheel.firestore.core.FirestoreUserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.*

class NotificationsViewModel : ViewModel() {

    private val repository = FirestoreUserManager()

    val events = repository.getEventsPaged(repository.currentUserId!!).toLiveData(20)

    fun acceptRequest(connection: Connection) {
        viewModelScope.launch {
            val event = Event.FollowAcceptedEvent(
                "",
                connection.fromUserId,
                Date(System.currentTimeMillis()),
                "FollowAcceptedEvent",
                false,
                repository.currentUserId!!
            )
            repository.updateConnection(connection.copy(state = "confirmed"))
                .combine(repository.insertEvent(event)) { c, e -> }
                .catch { }
                .collect { }
        }
    }

    fun markAsDone(event: Event) {
        event.done = true
        CoroutineScope(Dispatchers.IO).launch {
            repository.updateEvent(event).catch { }.collect { }
        }
    }
}