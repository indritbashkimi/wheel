package com.ibashkimi.wheel.firebase.model

data class FirebasePosition(
    var latitude: Double,
    var longitude: Double,
    var address: String?
) {
    constructor() : this(0.0, 0.0, null)
}