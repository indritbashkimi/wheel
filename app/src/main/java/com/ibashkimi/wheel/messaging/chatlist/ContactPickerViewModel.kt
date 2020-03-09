package com.ibashkimi.wheel.messaging.chatlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.ibashkimi.wheel.core.User
import com.ibashkimi.wheel.core.model.messaging.Room
import com.ibashkimi.wheel.firestore.core.FirestoreUserManager
import com.ibashkimi.wheel.firestore.messaging.FirestoreMessagingDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class ContactPickerViewModel : ViewModel() {

    private val repository = FirestoreUserManager()

    val users: LiveData<PagedList<User>> = repository.getAllUsersPaged().toLiveData(20)

    val selectedChat = MutableLiveData<String>()

    fun createChat(selectedUser: User) {
        val first = repository.currentUserId!!
        val second = selectedUser.uid
        val chatId = if (first < second) "$first$second" else "$second$first"
        CoroutineScope(Dispatchers.IO).launch {
            FirestoreMessagingDataManager().getChat(chatId).take(1)
                .flatMapLatest {
                    if (it == null) {
                        // Chat doesn't exist
                        val chat = Room(
                            uid = chatId,
                            imageUrl = selectedUser.imageUrl,
                            name = selectedUser.displayName ?: "",
                            createdAt = Date(),
                            creator = repository.currentUserId!!,
                            participants = listOf(repository.currentUserId!!, selectedUser.uid),
                            lastUpdated = Date(),
                            lastMessageId = null
                        )
                        FirestoreMessagingDataManager().createChat(chat)
                    } else {
                        // Chat exists
                        flowOf(it.uid)
                    }
                }.catch { }.collect {
                    selectedChat.postValue(it)
                }
        }
    }
}