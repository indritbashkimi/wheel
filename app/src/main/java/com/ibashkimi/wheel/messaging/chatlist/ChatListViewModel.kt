package com.ibashkimi.wheel.messaging.chatlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.ibashkimi.wheel.core.model.messaging.Room
import com.ibashkimi.wheel.firestore.core.FirestoreUserManager
import com.ibashkimi.wheel.firestore.messaging.FirestoreMessagingDataManager


class ChatListViewModel : ViewModel() {

    private val repository = FirestoreMessagingDataManager()

    val chats: LiveData<PagedList<Room>> =
        repository.getChatListPaged(FirestoreUserManager().currentUserId!!).toLiveData(20)
}