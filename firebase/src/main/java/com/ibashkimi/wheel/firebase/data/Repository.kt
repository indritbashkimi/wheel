package com.ibashkimi.wheel.firebase.data


object Repository {

    // Use dependency injection
    /*private val source: DataSource = FirebaseDataSource

    val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser!!.uid

    fun addFollowing(userId: String, followingId: String) =
        source.addFollowing(userId, followingId)

    fun removeFollowing(userId: String, followingId: String) =
        source.removeFollowing(userId, followingId)

    fun getFollowing(userId: String): Flow<List<User>> = source.getFollowing(userId)

    fun createChat(chat: Chat): Flow<Chat> = source.createChat(chat)

    fun saveMessage(chatId: String, message: Message) = source.saveMessage(chatId, message)

    fun getChat(id: String) = source.getChat(id)

    fun getUserChats(userId: String) = source.getUserChats(userId)

    fun messages(chatId: String) = source.messages(chatId)

    fun getMessage(id: String) = source.getMessage(id)

    fun createUser(user: User) = source.createUser(user)

    fun deleteComment(comment: Comment) = source.deleteComment(comment)

    fun deletePost(post: com.ibashkimi.orangestone.posts.Post) = source.deletePost(post)

    fun getComment(id: String) = source.getComment(id)

    fun getConnection(fromUserId: String, toUserId: String) =
        source.getConnection(fromUserId, toUserId)

    fun getConnections(userId: String) = source.getConnections(userId)

    fun getEvents(userId: String) = source.getEvents(userId)

    fun getPostComments(postId: String) = source.getPostComments(postId)

    fun getPost(id: String) = source.getPost(id)

    fun allPosts() = source.allPosts()

    fun getUser(id: String) = source.getUser(id)

    fun getAllUsers() = source.getAllUsers()

    fun getUserPosts(userId: String) = source.getUserPosts(userId)

    fun putComment(comment: Comment) = source.putComment(comment)

    fun saveConnection(connection: Connection) = source.saveConnection(connection)

    fun putPost(post: com.ibashkimi.orangestone.posts.Post) = source.putPost(post)

    fun updateEvent(event: Event) = source.updateEvent(event)*/
}
