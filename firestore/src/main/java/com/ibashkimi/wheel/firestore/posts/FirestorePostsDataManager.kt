package com.ibashkimi.wheel.firestore.posts

import androidx.paging.DataSource
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.ibashkimi.wheel.core.CachedData
import com.ibashkimi.wheel.core.Direction
import com.ibashkimi.wheel.core.User
import com.ibashkimi.wheel.core.data.PostsDataManager
import com.ibashkimi.wheel.core.model.core.Content
import com.ibashkimi.wheel.core.model.posts.*
import com.ibashkimi.wheel.firestore.*
import com.ibashkimi.wheel.firestore.core.FirestoreUserManager
import com.ibashkimi.wheel.firestore.core.toLike
import com.ibashkimi.wheel.firestore.core.toType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.*

class FirestorePostsDataManager : BaseFirestoreManager(), PostsDataManager {

    private val userRepository = FirestoreUserManager()

    val currentUserId: String? get() = currentUser?.uid

    override fun deletePost(post: Post): Flow<Unit> = db
        .collection("posts").document(post.uid)
        .delete().asFlow()

    override fun putComment(comment: Comment): Flow<String> =
        db.collection("posts").document(comment.postId)
            .collection("comments").document().writeFlow(
                mutableMapOf(
                    "contentType" to comment.content.toType(),
                    "createdAt" to comment.createdAt,
                    "postId" to comment.postId,
                    "userId" to comment.userId
                ).apply {
                    when (val content = comment.content) {
                        is Content.Text -> this["contentText"] = content.text
                        is Content.Media -> {
                            this["contentUri"] = content.uri
                            content.text?.let { this["contentText"] = it }
                        }
                        is Content.Unsupported -> throw IllegalStateException("Trying to send unsupported content.")
                    }
                }
            )

    override fun getComment(postId: String, commentId: String): Flow<Comment?> =
        db.collection("posts").document(postId).collection("comments")
            .document(commentId)
            .asFlow()
            .map { it.toComment() }

    override fun deleteComment(comment: Comment): Flow<Unit> =
        db.collection("posts").document(comment.postId).collection("comments")
            .document(comment.uid)
            .delete().asFlow()

    /*fun getComments(postId: String): Flow<List<Comment>> =
        db.collection("posts").document(postId)
            .collection("comments")
            .orderBy("createdAt")
            .asFlow()
            .map { it.toCommentList() }*/

    override fun getComments(
        postId: String,
        limit: Long,
        direction: Direction,
        continuation: Any?
    ): Flow<List<Comment>> =
        db.collection("posts").document(postId)
            .collection("comments")
            .orderBy("createdAt", direction.toFirestoreDirection()) // todo rename in created
            .withContinuation(continuation)
            .limit(limit)
            .asFlow()
            .map { it.toCommentList() }

    override fun getCommentsPaged(postId: String): DataSource.Factory<Any, Comment> {
        val query = db.collection("posts").document(postId)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.DESCENDING)
        // TODO: find a way to reuse cachedUser?
        // Probably CommentWithUser should be somewhere in UseCases or ViewModel
        val cachedUser = CachedData {
            userRepository.getUser(it)
        }
        return FirestoreDataSourceFactory(
            query
        ) {
            it.map { query ->
                val comment = query.toComment()
                comment.user = cachedUser.getItem(comment.userId)!!
                comment
            }
        } as DataSource.Factory<Any, Comment>
    }

    fun addLikeToComment(comment: Comment): Flow<String> =
        db.collection("posts").document(comment.postId)
            .collection("comments").document(comment.uid)
            .collection("likes").document(currentUserId!!).writeFlow(
                hashMapOf(
                    "fromUserId" to currentUserId!!,
                    "created" to System.currentTimeMillis(),
                    "contentId" to comment.uid
                )
            )

    fun removeLikeFromComment(comment: Comment): Flow<Unit> =
        db.collection("posts").document(comment.postId)
            .collection("comments").document(comment.uid)
            .collection("likes").document(currentUserId!!)
            .delete().asFlow()

    fun getLikeToComment(comment: Comment, userId: String): Flow<Like?> =
        db.collection("posts").document(comment.postId)
            .collection("comments").document(comment.uid)
            .collection("likes").document(userId)
            .asFlow()
            .map { it.toLike() }

    fun hasUserLikedComment(comment: Comment): Flow<Boolean> =
        getLikeToComment(comment, currentUserId!!).map { it != null }

    fun addLikeToPost(postId: String): Flow<String> =
        db.collection("posts").document(postId)
            .collection("likes").document(currentUserId!!).writeFlow(
                hashMapOf(
                    "fromUserId" to currentUserId!!,
                    "created" to System.currentTimeMillis(),
                    "contentId" to postId
                )
            )

    fun removeLikeFromPost(postId: String): Flow<Unit> =
        db.collection("posts").document(postId)
            .collection("likes").document(currentUserId!!)
            .delete().asFlow()

    fun getLikeToPost(postId: String, userId: String): Flow<Like?> =
        db.collection("posts").document(postId)
            .collection("likes").document(userId)
            .asFlow()
            .map { it.toLike() }

    fun hasUserLikedPost(postId: String): Flow<Boolean> =
        getLikeToPost(postId, currentUserId!!).map { it != null }


    override fun getPost(id: String): Flow<Post?> =
        db.collection("posts").document(id)
            .asFlow()
            .map { it.toPost() }
            .combine(hasUserLikedPost(id)) { post, liked ->
                android.util.Log.d("FirebaseDataManager", "post: ${post.uid}, liked: ${liked}")
                post.liked = liked
                post
            }
            .catch {
                it.printStackTrace()
                android.util.Log.d("FirebaseDataManager", "load post e: ${it.message}")
            }

    /*fun getAllPosts(): Flow<List<Post>> =
        db.collection("posts")
            .orderBy("created", Query.Direction.DESCENDING)
            .asFlow()
            .map { it.toPostList() }*/

    override fun getAllPosts(
        limit: Long,
        direction: Direction,
        continuation: Any?
    ): Flow<List<Post>> =
        db.collection("posts")
            .orderBy("created", direction.toFirestoreDirection())
            .withContinuation(continuation)
            .limit(limit)
            .asFlow()
            .map { it.toPostList() }

    override fun getAllPostsPaged(): DataSource.Factory<Any, UserPost> {
        val query = db.collection("posts")
            .orderBy("created", Query.Direction.DESCENDING)
        val cachedUsers = CachedData {
            userRepository.getUser(it)
        }
        return FirestoreDataSourceFactory(query) {
            it.map { query ->
                val post = query.toPost()
                val user: User = cachedUsers.getItem(post.userId)!!
                UserPost(user, post)
            }
        } as DataSource.Factory<Any, UserPost>
    }

    override fun getUserPosts(
        userId: String, limit: Long,
        direction: Direction,
        continuation: Any?
    ): Flow<List<Post>> = db
        .collection("posts")
        .whereEqualTo("userId", userId)
        .orderBy("created", Query.Direction.DESCENDING)
        .withContinuation(continuation)
        .asFlow()
        .map { it.toPostList() }

    override fun getUserPostsPaged(userId: String): DataSource.Factory<Any, UserPost> {
        val query = db.collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("created", Query.Direction.DESCENDING)
        val cachedUsers = CachedData {
            userRepository.getUser(it)
        }
        return FirestoreDataSourceFactory(query) {
            it.map { query ->
                val post = query.toPost()
                val user: User = cachedUsers.getItem(post.userId)!!
                UserPost(user, post)
            }
        } as DataSource.Factory<Any, UserPost>
    }

    override fun putPost(post: Post): Flow<String> =
        db.collection("posts").document().writeFlow(
            mutableMapOf(
                "userId" to post.userId,
                "position" to post.position?.let {
                    GeoPoint(
                        it.latitude,
                        it.longitude
                    )
                },
                "latitude" to post.position?.latitude,
                "longitude" to post.position?.longitude, // todo unify fields of position?
                "address" to post.position?.address,
                "created" to Date(post.created),
                "contentType" to post.content.toType()
            ).apply {
                when (val content = post.content) {
                    is Content.Text -> this["contentText"] = content.text
                    is Content.Media -> {
                        this["contentUri"] = content.uri
                        content.text?.let { this["contentText"] = it }
                    }
                    is Content.Unsupported -> throw IllegalStateException("Trying to send unsupported content.")
                }
            }
        )
}