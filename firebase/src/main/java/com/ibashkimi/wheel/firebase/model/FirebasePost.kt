package com.ibashkimi.wheel.firebase.model


import com.google.firebase.database.Exclude

data class FirebasePost(
    @Exclude override var uid: String,
    var userId: String,
    var position: FirebasePosition?,
    var created: Long,
    var content: Content?
) : BaseFirebaseModel(uid) {

    constructor() : this(
        "", "",
        com.ibashkimi.wheel.firebase.model.FirebasePosition(), 0, null
    )
}
