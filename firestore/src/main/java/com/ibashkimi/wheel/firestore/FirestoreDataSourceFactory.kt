package com.ibashkimi.wheel.firestore

import androidx.paging.DataSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class FirestoreDataSourceFactory<T>(
    val query: Query,
    val mapper: suspend (QuerySnapshot) -> List<T>
) : DataSource.Factory<DocumentSnapshot, T>() {

    override fun create(): DataSource<DocumentSnapshot, T> {
        return FirestoreDataSource(query, mapper)
    }
}
