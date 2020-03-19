package com.ibashkimi.wheel.firestore.posts

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.ibashkimi.wheel.core.model.posts.Comment
import com.ibashkimi.wheel.core.model.posts.Position
import com.ibashkimi.wheel.core.model.posts.Post
import com.ibashkimi.wheel.firestore.core.toContent

fun QuerySnapshot.toPostList(): List<Post> = this.map { it.toPost() }

fun DocumentSnapshot.toPost(): Post {
    val content = toContent(getString("contentType") ?: "unsupported")
    val latitude = getDouble("latitude") // todo handle position in a better way
    val longitude = getDouble("longitude")
    val address = getString("address")
    val position =
        if (latitude != null && longitude != null) Position(
            latitude,
            longitude,
            address
        ) else null
    return Post(
        uid = id,
        userId = getString("userId")!!,
        position = position,
        created = getDate("created")!!.time,
        content = content
    )
}

fun QuerySnapshot.toCommentList(): List<Comment> = this.map { it.toComment() }

fun DocumentSnapshot.toComment() = Comment(
    uid = id,
    content = toContent(getString("contentType") ?: "unsupported"),
    createdAt = getLong("createdAt")!!,
    postId = getString("postId")!!,
    userId = getString("userId")!!
)