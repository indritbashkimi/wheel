package com.ibashkimi.wheel.posts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.ibashkimi.wheel.core.Direction
import com.ibashkimi.wheel.core.model.posts.UserPost
import com.ibashkimi.wheel.firestore.posts.FirestorePostsDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

class PostsViewModel(app: Application) : AndroidViewModel(app) {

    private val repository =
        FirestorePostsDataManager()

    val posts: LiveData<List<UserPost>> =
        repository.getAllPosts(1000, Direction.DESCENDING, null).map {
            it.map { post ->
                UserPost(
                    PostsUseCases.getUser(post.userId)!!, post
                )
            }
        }.asAltScopedLiveData(viewModelScope)

    val postsPaged: LiveData<PagedList<UserPost>> = repository.getAllPostsPaged().toLiveData(50)

    private fun <T> Flow<T>.asAltScopedLiveData(scope: CoroutineScope): LiveData<T> {
        val liveData = MutableLiveData<T>()
        this
            .onEach { liveData.value = it }
            .catch {
                it.printStackTrace()
            }
            .launchIn(scope)
        return liveData
    }
}

