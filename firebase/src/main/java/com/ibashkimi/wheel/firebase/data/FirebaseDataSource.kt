package com.ibashkimi.wheel.firebase.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ibashkimi.wheel.core.User
import com.ibashkimi.wheel.core.model.core.Connection
import com.ibashkimi.wheel.core.model.messaging.Message
import com.ibashkimi.wheel.core.model.posts.Comment
import com.ibashkimi.wheel.core.model.posts.Post
import com.ibashkimi.wheel.firebase.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
internal object FirebaseDataSource :
    DataSource {

    override val currentUserId: String = FirebaseAuth.getInstance().currentUser!!.uid

    private val database = FirebaseDatabase.getInstance()

    override fun allPosts(): Flow<List<Post>> = "/posts"
        .listedAsFlow<FirebasePost>()
        .flowOn(Dispatchers.IO)
        .map { it.mapToPosts() }
        .flowOn(Dispatchers.Default)

    override fun getPost(id: String): Flow<Post?> = nullableDataFlow<FirebasePost>("/posts/$id")
        .flowOn(Dispatchers.IO)
        .map { it?.mapToDomain() }

    override fun getFollowing(userId: String): Flow<List<User>> =
        getConnections(userId).flatMapLatest { connections ->
            flow {
                val users = ArrayList<User>()
                // is confirmed && it.fromUserId == userId
                connections.filter { it.fromUserId == userId }.forEach {
                    getUser(it.toUserId)
                        .filterNotNull().collect { user ->
                            users.add(user)
                        }
                }
                emit(users)
            }
        }.flowOn(Dispatchers.IO)

    override fun getChat(id: String): Flow<Chat?> = nullableDataFlow<FirebaseChat>("chats/$id")
        .flowOn(Dispatchers.IO)
        .map { it?.mapToDomain() }

    override fun getUserChats(userId: String): Flow<List<Chat>> =
        listedDataFlow<FirebaseChat>("/user-chats/$userId")
            .flowOn(Dispatchers.IO)
            .map { it.mapToChats() }
            .flowOn(Dispatchers.Default)

    override fun messages(chatId: String): Flow<List<Message>> =
        listedDataFlow<FirebaseMessage>("/chat-messages/$chatId")
            .flowOn(Dispatchers.IO)
            .map { it.mapToMessages() }
            .flowOn(Dispatchers.Default)

    override fun getMessage(id: String): Flow<Message?> =
        nullableDataFlow<FirebaseMessage>("/messages/$id")
            .flowOn(Dispatchers.IO)
            .map { it?.mapToDomain() }

    override fun getComment(id: String): Flow<Comment?> =
        nullableDataFlow<FirebaseComment>("/comments/$id")
            .flowOn(Dispatchers.IO)
            .map { it?.mapToDomain() }

    override fun getConnections(userId: String): Flow<List<Connection>> =
        listedDataFlow<FirebaseConnection>("/connections/$userId")
            .flowOn(Dispatchers.IO)
            .map { it.mapToConnections() }
            .flowOn(Dispatchers.Default)

    override fun getEvents(userId: String): Flow<List<Notification>> =
        snapshotFlow("/user-events/$userId/").map { snapshot ->
            val events = ArrayList<FirebaseEvent>()
            snapshot.children.forEach {
                val event: FirebaseEvent = it.getValue(FirebaseEvent::class.java)
                    ?: throw Exception("Cannot deserialize event.")
                event.uid = snapshot.key!!
                event.data = when (event.type) {
                    "connection" -> {
                        it.child("data").getValue(FirebaseConnection::class.java)
                    }
                    else -> throw Exception()
                }
                events.add(event)
            }
            events
        }.flowOn(Dispatchers.IO).map { it.mapToEvents() }.flowOn(Dispatchers.Default)

    override fun getPostComments(postId: String): Flow<List<Comment>> =
        listedDataFlow<FirebaseComment>("/post-comments/$postId/")
            .flowOn(Dispatchers.IO)
            .map { it.mapToComments() }
            .flowOn(Dispatchers.Default)

    override fun getUser(id: String): Flow<User?> = nullableDataFlow<FirebaseUser>("/users/$id")
        .flowOn(Dispatchers.IO)
        .map { it?.mapToDomain() }

    override fun getAllUsers(): Flow<List<User>> = listedDataFlow<FirebaseUser>("/users")
        .flowOn(Dispatchers.IO)
        .map { it.mapToUsers() }
        .flowOn(Dispatchers.Default)

    override fun getUserPosts(userId: String): Flow<List<Post>> =
        listedDataFlow<FirebasePost>("/user-posts/$userId")
            .flowOn(Dispatchers.IO)
            .map { it.mapToPosts() }
            .flowOn(Dispatchers.Default)

    override fun createChat(chat: Chat): Flow<Chat> = writeFlow(chat) {
        val ref: DatabaseReference = database.reference
        // Generate a new push ID for the new post
        val newChatRef = ref.child("chats").push()
        val newChatKey = newChatRef.key
        /*val users = chat.users
        val newChatKey = if (usersIds[0].uid < usersIds[1].uid) {
            usersIds[0].uid + usersIds[1].uid
        } else usersIds[1] + usersIds[0]*/
        val newChat = chat.mapToFirebase()
        newChat.uid = newChatKey!!
        // Create the data we want to update
        val map = HashMap<String, Any>()
        map["chats/$newChatKey"] = newChat
        map["user-chats/${chat.usersIds!![0]}/$newChatKey"] = newChat
        map["user-chats/${chat.usersIds!![1]}/$newChatKey"] = newChat
        map
    }.flowOn(Dispatchers.IO)

    override fun saveMessage(chatId: String, message: Message): Flow<Message> = writeFlow(message) {
        val ref: DatabaseReference = database.reference
        // Generate a new push ID for the new post
        val newMessageRef = ref.child("messages").push()
        val newMessageKey = newMessageRef.key
        // Create the data we want to update
        val newMessage: FirebaseMessage = message.mapToFirebase()
        val map = HashMap<String, Any>()
        map["messages/$newMessageKey"] = newMessage
        map["chats/${message.chatId}/messages/$newMessageKey"] = true
        map
    }.flowOn(Dispatchers.IO)

    override fun addFollowing(userId: String, followingId: String): Flow<Boolean> =
        writeFlow(true) {
            val map = HashMap<String, Any>()
            map["/users/$userId/following/$followingId/"] = true
            map
        }.flowOn(Dispatchers.IO)

    override fun removeFollowing(userId: String, followingId: String): Flow<Boolean> =
        writeNullFlow(true) {
            val map = HashMap<String, Any?>()
            map["/users/$userId/following/$followingId"] = null
            map
        }.flowOn(Dispatchers.IO)


    override fun createUser(user: User): Flow<User> = writeFlow(user) {
        val ref = database.getReference("users")
        val now: Long = System.currentTimeMillis()
        val firebaseUser = FirebaseUser(
            user.uid,
            user.displayName,
            user.email,
            null,
            user.imageUrl, //user.photoUrl.toString(),
            null,
            0,
            0,
            now,
            now
        )
        val map = HashMap<String, Any>()
        map[user.uid] = firebaseUser
        map
    }.flowOn(Dispatchers.IO)

    override fun deleteComment(comment: Comment): Flow<Comment> = writeNullFlow(comment) {
        // Create the data we want to update
        val map = HashMap<String, Any?>()
        map["posts/${comment.postId}/comments/${comment.uid}"] = null
        map["comments/${comment.uid}"] = null
        // Do a deep-path update
        map
    }.flowOn(Dispatchers.IO)

    override fun deletePost(post: Post): Flow<Post> = writeNullFlow(post) {
        // Create the data we want to update
        val map = HashMap<String, Any?>()
        map["users/${post.userId}/posts/${post.uid}"] = null
        map["posts/${post.uid}"] = null
        map
    }.flowOn(Dispatchers.IO)

    override fun getConnection(fromUserId: String, toUserId: String): Flow<Connection?> =
        getConnections(fromUserId).map {
            it.firstOrNull { connection -> connection.toUserId == toUserId }
        }.flowOn(Dispatchers.IO)

    fun getConnection(connId: String): Flow<Connection> =
        dataFlow<FirebaseConnection>("/connections/$connId")
            .flowOn(Dispatchers.IO)
            .map { it.mapToDomain() }

    override fun putComment(comment: Comment): Flow<Comment> = writeFlow(comment) {
        val ref: DatabaseReference = database.reference
        // Generate a new push ID for the new post
        val newCommentRef = ref.child("comments").push()
        val newCommentKey = newCommentRef.key
        // Create the data we want to update
        val newComment = comment.mapToFirebase()
        val map = HashMap<String, Any>()
        map["posts/${comment.postId}/comments/$newCommentKey"] = true
        map["comments/$newCommentKey"] = newComment
        map
    }.flowOn(Dispatchers.IO)

    override fun saveConnection(connection: Connection): Flow<Connection> = writeFlow(connection) {
        val ref: DatabaseReference = database.reference
        // Generate a new push ID for the new connection
        val newConnRef = ref.child("connections").push()
        val newConnKey = newConnRef.key
        val newConnection = connection.mapToFirebase()
        // Create the data we want to update
        val map = HashMap<String, Any>()
        map["connections/$newConnKey"] = newConnection
        map["users/${newConnection.fromUserId}/connections/$newConnKey"] = true
        map["users/${newConnection.toUserId}/connections/$newConnKey"] = true

        // Generate a new push ID for the new connection
        val newEventRef = ref.child("events").push()
        val newEventKey = newEventRef.key
        val event = FirebaseEvent().apply {
            createdAt = connection.created
            type = "connection"
            uid = newEventKey!!
            objectUid = newConnKey
            data = connection
            //data = connection
        }
        map["events/$newEventKey"] = event
        //updatedUserData.put("events/$newEventKey/data", event.data)
        map["users/${connection.toUserId}/events/$newEventKey"] = true

        map
    }.flowOn(Dispatchers.IO)

    override fun putPost(post: Post): Flow<Post> = writeFlow(post) {
        //https://firebase.googleblog.com/2015/09/introducing-multi-location-updates-and_86.html
        val ref: DatabaseReference = database.reference
        // Generate a new push ID for the new post
        val newPostRef = ref.child("posts").push()
        val newPostKey = newPostRef.key
        val newPost = post.mapToFirebase()
        // Create the data we want to update
        val map = HashMap<String, Any>()
        map["user-posts/${post.userId}/$newPostKey"] = newPost
        map["posts/$newPostKey"] = newPost
        map
    }.flowOn(Dispatchers.IO)

    override fun updateEvent(event: Notification): Flow<Notification> = writeFlow(event) {
        val map = HashMap<String, Any>()
        val firebaseEvent = event.mapToFirebase()
        val eventMap = firebaseEvent.toMap()
        when (firebaseEvent.type) {
            "connection" -> {
                val connection = firebaseEvent.data as FirebaseConnection
                connection.uid = firebaseEvent.objectUid!!
                eventMap["data"] = connection.toMap()
                map["connections/${connection.uid}"] = connection
                map["events/${firebaseEvent.uid}/"] = eventMap
                // todo use userId somehow
                map["user-events/${connection.toUserId}/${firebaseEvent.uid}"] = eventMap
            }
        }
        map
    }.flowOn(Dispatchers.IO)

    class NotDeserializableItem : IllegalStateException("Cannot deserialize element.")

    private inline fun <reified T : BaseFirebaseModel> listedDataFlow(ref: String): Flow<List<T>> =
        snapshotFlow(ref).map<DataSnapshot, List<T>> { deserializeAsListed(it) }

    private inline fun <reified T : BaseFirebaseModel> deserializeAsListed(snapshot: DataSnapshot): List<T> {
        val elements = ArrayList<T>(snapshot.childrenCount.toInt())
        snapshot.children.forEach {
            elements.add(it.deserialize())
        }
        return elements
    }

    private inline fun <reified T : BaseFirebaseModel> DataSnapshot.deserialize(): T {
        val elem: T = this.getValue(T::class.java) ?: throw NotDeserializableItem()
        elem.uid = this.key ?: throw NotDeserializableItem()
        return elem
    }

    private inline fun <reified T : BaseFirebaseModel> DataSnapshot.deserializeNullable(): T? {
        val uid = this.key ?: return null
        val elem: T? = this.getValue(T::class.java)
        elem?.uid = uid
        return elem
    }

    private inline fun <reified T : BaseFirebaseModel> dataFlow(ref: String): Flow<T> =
        snapshotFlow(ref).map { it.deserialize<T>() }

    private inline fun <reified T : BaseFirebaseModel> nullableDataFlow(ref: String): Flow<T?> =
        snapshotFlow(ref).map { it.deserializeNullable<T>() }

    private fun snapshotFlow(reference: String): Flow<DataSnapshot> {
        return callbackFlow {
            val ref = FirebaseDatabase.getInstance().getReference(reference)
            ref.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                    close(databaseError.toException())
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    offer(snapshot)
                }
            })

        }
    }

    private inline fun <reified T> writeFlow(
        data: T,
        crossinline toMap: (T) -> HashMap<String, Any>
    ): Flow<T> = callbackFlow {
        val ref = database.reference
        val listener = DatabaseReference.CompletionListener { databaseError, _ ->
            if (databaseError == null) {
                offer(data)
                close()
            } else {
                close(databaseError.toException())
            }
        }

        ref.updateChildren(toMap(data), listener)
        awaitClose()
    }

    private inline fun <reified T> writeNullFlow(
        data: T,
        crossinline toMap: (T) -> HashMap<String, Any?>
    ): Flow<T> = callbackFlow {
        val ref = database.reference
        val listener = DatabaseReference.CompletionListener { databaseError, _ ->
            if (databaseError == null) {
                offer(data)
                close()
            } else {
                close(databaseError.toException())
            }
        }

        ref.updateChildren(toMap(data), listener)
        awaitClose()
    }

    inline fun <reified T : BaseFirebaseModel> String.listedAsFlow(): Flow<List<T>> {
        return listedDataFlow(this)
    }
}