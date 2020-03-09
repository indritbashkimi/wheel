package com.ibashkimi.wheel.postdetail

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.ibashkimi.wheel.core.User
import com.ibashkimi.wheel.core.model.posts.Comment
import com.ibashkimi.wheel.core.model.posts.Post
import com.ibashkimi.wheel.firestore.core.FirestoreUserManager
import com.ibashkimi.wheel.firestore.posts.FirestorePostsDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PostDetailViewModel(postId: String) : ViewModel() {

    private val repository = FirestorePostsDataManager()

    val post = MutableLiveData<Post>()

    val comments: LiveData<PagedList<Comment>> =
        repository.getCommentsPaged(postId).toLiveData(50)

    val user = MutableLiveData<User>()

    init {
        FirestorePostsDataManager()
            .getPost(postId).filterNotNull().onEach { post.postValue(it) }
            .flatMapLatest { FirestoreUserManager().getUser(it.userId) }
            .onEach { user.postValue(it) }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }

    fun likePost() {
        post.value?.let { post ->
            viewModelScope.launch {
                repository.addLikeToPost(post.uid)
                    .catch {
                        Log.d("PostDetailViewModel", "error, post not liked")
                    }
                    .collect {
                        Log.d("PostDetailViewModel", "post liked ${post.uid}")
                    }
            }
        }
    }

    fun deletePost(post: Post) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.deletePost(post)
                .catch { Log.d("PostDetailViewModel", "Cannot delete post") }
                .collect { Log.d("PostDetailViewModel", "Post deleted") }
        }
    }

    fun saveComment(comment: Comment) {
        repository.putComment(comment)
            .catch {
                Log.d("PostDetailViewModel", "Error, cannot save comment.")
            }
            .onEach {
                Log.d("PostDetailViewModel", "Comment saved")
            }.launchIn(viewModelScope)
    }

    fun deleteComment(comment: Comment) {
        repository.deleteComment(comment)
            .catch {
                Log.d("PostDetailViewModel", "Error, cannot delete comment.")
            }
            .onEach {
                Log.d("PostDetailViewModel", "Comment deleted")
            }.launchIn(viewModelScope)
    }

    class Factory(private val postId: String) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PostDetailViewModel(postId) as T
        }
    }
}