package com.ibashkimi.wheel.firebase.model


import com.google.firebase.database.Exclude
import com.ibashkimi.wheel.core.model.posts.Content

data class FirebasePost(
    @Exclude override var uid: String,
    var userId: String,
    var position: com.ibashkimi.wheel.firebase.model.FirebasePosition?,
    var created: Long,
    var content: Content?
) : BaseFirebaseModel(uid) {

    constructor() : this(
        "", "",
        com.ibashkimi.wheel.firebase.model.FirebasePosition(), 0, null
    )
}
