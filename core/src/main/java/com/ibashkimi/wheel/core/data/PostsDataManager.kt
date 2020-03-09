package com.ibashkimi.wheel.core.data

import androidx.paging.DataSource
import com.ibashkimi.wheel.core.Direction
import com.ibashkimi.wheel.core.model.posts.Comment
import com.ibashkimi.wheel.core.model.posts.Post
import com.ibashkimi.wheel.core.model.posts.UserPost
import kotlinx.coroutines.flow.Flow

interface PostsDataManager {
    // Posts

    fun getPost(id: String): Flow<Post?>

    fun putPost(post: Post): Flow<String>

    fun deletePost(post: Post): Flow<Unit>

    fun getAllPosts(limit: Long, direction: Direction, continuation: Any?): Flow<List<Post>>

    fun getAllPostsPaged(): DataSource.Factory<Any, UserPost>

    fun getUserPosts(
        userId: String,
        limit: Long,
        direction: Direction,
        continuation: Any?
    ): Flow<List<Post>>

    fun getUserPostsPaged(userId: String): DataSource.Factory<Any, UserPost>

    // Comments

    fun getComment(postId: String, commentId: String): Flow<Comment?>

    fun putComment(comment: Comment): Flow<String>

    fun deleteComment(comment: Comment): Flow<Unit>

    fun getComments(
        postId: String,
        limit: Long,
        direction: Direction,
        continuation: Any?
    ): Flow<List<Comment>>

    fun getCommentsPaged(postId: String): DataSource.Factory<Any, Comment>
}