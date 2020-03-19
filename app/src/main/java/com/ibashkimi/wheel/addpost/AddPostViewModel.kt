package com.ibashkimi.wheel.addpost

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.ibashkimi.wheel.core.data.PostsDataManager
import com.ibashkimi.wheel.core.model.core.Content
import com.ibashkimi.wheel.core.model.posts.Position
import com.ibashkimi.wheel.core.model.posts.Post
import com.ibashkimi.wheel.firestore.posts.FirestorePostsDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AddPostViewModel : ViewModel() {

    private val repository: PostsDataManager =
        FirestorePostsDataManager()

    val text = MutableLiveData<String?>()

    val position = MutableLiveData<Position?>(null)

    val image = MutableLiveData<String?>(null)

    fun isValidForSave(): Boolean {
        return text.value != null || position.value != null || image.value != null
    }

    fun savePost() {
        CoroutineScope(Dispatchers.IO).launch {
            val pos = position.value
            val content = text.value ?: ""
            val post = Post(
                uid = "",
                userId = FirebaseAuth.getInstance().currentUser!!.uid,
                position = pos,
                created = System.currentTimeMillis(),
                content = Content.Text(content)
            )
            repository
                .putPost(post)
                .catch {
                    android.util.Log.d("AddPostViewModel", "catch: ${it.message}")
                }
                .collect {
                    android.util.Log.d("AddPostViewModel", "success")
                }
        }
    }
}