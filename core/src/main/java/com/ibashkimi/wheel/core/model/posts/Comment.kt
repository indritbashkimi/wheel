package com.ibashkimi.wheel.core.model.posts

import com.ibashkimi.wheel.core.User
import com.ibashkimi.wheel.core.model.core.Content

data class Comment(
    val uid: String,
    val content: Content,
    val createdAt: Long,
    val postId: String,
    val userId: String,
    var user: User? = null
)