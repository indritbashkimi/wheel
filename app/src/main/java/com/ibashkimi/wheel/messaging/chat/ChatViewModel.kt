package com.ibashkimi.wheel.messaging.chat

import androidx.lifecycle.*
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.ibashkimi.wheel.core.User
import com.ibashkimi.wheel.core.model.core.Content
import com.ibashkimi.wheel.core.model.messaging.Message
import com.ibashkimi.wheel.firestore.core.FirestoreUserManager
import com.ibashkimi.wheel.firestore.messaging.FirestoreMessagingDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(chatId: String) : ViewModel() {

    private val repository = FirestoreMessagingDataManager()

    private val userManager = FirestoreUserManager()

    val userId = userManager.currentUserId!!

    val otherUser: LiveData<User> = repository.getChat(chatId).filterNotNull()
        .flatMapLatest { room ->
            room.participants.firstOrNull { it != userManager.currentUserId }?.let {
                userManager.getUser(it)
            } ?: flowOf(null)
        }.catch { }.filterNotNull().asLiveData(Dispatchers.IO)

    val messagesPaged: LiveData<PagedList<Message>> =
        repository.getMessagesPaged(chatId).toLiveData(20)

    fun sendMessage(chatId: String, message: Content) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.createMessage(chatId, message)
                .catch { android.util.Log.d("ChatViewModel", "error sending message") }
                .collect {
                    android.util.Log.d("ChatViewModel", "message sent")
                }
        }
    }

    fun deleteChat(chatId: String) {
        repository.clearChat(chatId).combine(repository.deleteChat(chatId)) { _, _ -> }
            .catch { }.launchIn(CoroutineScope(Dispatchers.IO))
    }

    fun clearChat(chatId: String) {
        repository.clearChat(chatId).catch { }.launchIn(CoroutineScope(Dispatchers.IO))
    }

    fun deleteMessage(message: Message) {
        viewModelScope.launch {
            repository.deleteMessage(message.chatId, message.id).catch { }.collect { }
        }
    }

    class Factory(private val chatId: String) :
        ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ChatViewModel(chatId) as T
        }
    }
}