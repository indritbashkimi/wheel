package com.ibashkimi.wheel.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.ibashkimi.wheel.core.asScopedLiveData
import com.ibashkimi.wheel.core.model.core.Connection
import com.ibashkimi.wheel.core.model.core.Event
import com.ibashkimi.wheel.core.model.posts.UserPost
import com.ibashkimi.wheel.firestore.core.FirestoreUserManager
import com.ibashkimi.wheel.firestore.posts.FirestorePostsDataManager
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.*

class ProfileViewModel(userId: String) : ViewModel() {

    private val repository = FirestorePostsDataManager()

    val user = FirestoreUserManager().getUser(userId).asScopedLiveData(viewModelScope)

    val postsPaged: LiveData<PagedList<UserPost>> =
        repository.getUserPostsPaged(userId).toLiveData(50)

    val connection = FirestoreUserManager()
        .getConnection(repository.currentUserId!!, userId)
        .asScopedLiveData(viewModelScope)

    fun createConnection(toUser: String) {
        val now = System.currentTimeMillis()

        val connection = Connection(
            uid = null,
            fromUserId = repository.currentUserId!!,
            created = Date(now),
            state = "pending",
            toUserId = toUser,
            type = null
        )

        val event = Event.NewFollowRequestEvent(
            "",
            toUser,
            Date(now),
            "NewFollowRequestEvent",
            false,
            repository.currentUserId!!
        )

        viewModelScope.launch {
            FirestoreUserManager().insertConnection(connection)
                .combine(FirestoreUserManager().insertEvent(event)) { connection, event ->
                    Log.d("ProfileViewModel", "$connection $event")
                }
                .catch { }
                .collect { }
        }
    }

    fun deleteConnection(connection: Connection) {
        viewModelScope.launch {
            FirestoreUserManager().deleteConnection(connection)
                .catch { }
                .collect { }
        }
    }

    class Factory(private val userId: String) :
        ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ProfileViewModel(userId) as T
        }
    }
}