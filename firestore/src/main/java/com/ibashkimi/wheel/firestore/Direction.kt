package com.ibashkimi.wheel.firestore

import com.google.firebase.firestore.Query
import com.ibashkimi.wheel.core.Direction

fun Direction.toFirestoreDirection() = when (this) {
    Direction.ASCENDING -> Query.Direction.ASCENDING
    Direction.DESCENDING -> Query.Direction.DESCENDING
}