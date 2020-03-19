package com.ibashkimi.wheel.core.model.posts

import com.ibashkimi.wheel.core.model.core.Content

data class Post(
    val uid: String,
    val userId: String,
    val position: Position?,
    val created: Long,
    val content: Content,
    var liked: Boolean = false
)