package com.ibashkimi.wheel.posts

import com.ibashkimi.wheel.core.User
import com.ibashkimi.wheel.firestore.core.FirestoreUserManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import java.lang.ref.WeakReference

object PostsUseCases {

    private val repository = FirestoreUserManager()

    private val usersMap = HashMap<String, WeakReference<User?>>()

    suspend fun getUser(userId: String): User? {
        var user = usersMap[userId]?.get()
        if (user == null) {
            repository.getUser(userId).take(1).collect {
                user = it
                usersMap[userId] = WeakReference(it)
            }
        }
        return user
    }
}

