package com.ibashkimi.wheel.firebase.model

data class Notification(
    val uid: String,
    val createdAt: Long = 0,
    val type: String? = null,
    val objectUid: String? = null,
    val done: Boolean = false,
    val data: Any? = null
)