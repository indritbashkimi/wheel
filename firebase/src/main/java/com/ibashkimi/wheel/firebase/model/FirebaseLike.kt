package com.ibashkimi.wheel.firebase.model

import com.ibashkimi.wheel.core.model.posts.Like

data class FirebaseLike(var userId: String, var contentId: String, var created: Long) {
    constructor() : this("", "", 0)

    fun map() = Like(
        userId,
        contentId,
        created
    )
}