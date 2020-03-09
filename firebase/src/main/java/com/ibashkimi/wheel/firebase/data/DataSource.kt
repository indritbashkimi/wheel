package com.ibashkimi.wheel.firebase.data

import com.ibashkimi.wheel.core.User
import com.ibashkimi.wheel.core.model.core.Connection
import com.ibashkimi.wheel.core.model.messaging.Message
import com.ibashkimi.wheel.core.model.posts.Comment
import com.ibashkimi.wheel.core.model.posts.Post
import com.ibashkimi.wheel.firebase.model.Chat
import com.ibashkimi.wheel.firebase.model.Notification
import kotlinx.coroutines.flow.Flow

interface DataSource {

    val currentUserId: String?

    fun getPost(id: String): Flow<Post?>

    fun putPost(post: Post): Flow<Post>

    fun deletePost(post: Post): Flow<Post>

    fun getUserPosts(userId: String): Flow<List<Post>>

    fun getComment(id: String): Flow<Comment?>

    fun putComment(comment: Comment): Flow<Comment>

    fun deleteComment(comment: Comment): Flow<Comment>

    fun getUser(id: String): Flow<User?>

    fun createUser(user: User): Flow<User>

    // putUser updateUser deleteUser

    fun addFollowing(userId: String, followingId: String): Flow<Boolean>

    fun removeFollowing(userId: String, followingId: String): Flow<Boolean>

    fun getFollowing(userId: String): Flow<List<User>>

    fun createChat(chat: Chat): Flow<Chat>

    fun saveMessage(chatId: String, message: Message): Flow<Message>

    fun getChat(id: String): Flow<Chat?>

    fun getUserChats(userId: String): Flow<List<Chat>>

    fun messages(chatId: String): Flow<List<Message>>

    fun getMessage(id: String): Flow<Message?>

    fun getConnection(fromUserId: String, toUserId: String): Flow<Connection?>

    fun getConnections(userId: String): Flow<List<Connection>>

    fun getEvents(userId: String): Flow<List<Notification>>

    fun getPostComments(postId: String): Flow<List<Comment>>

    fun allPosts(): Flow<List<Post>>

    fun getAllUsers(): Flow<List<User>>

    fun saveConnection(connection: Connection): Flow<Connection>

    fun updateEvent(event: Notification): Flow<Notification>
}
