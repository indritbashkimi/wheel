package com.ibashkimi.wheel.firestore

import android.util.Log
import androidx.paging.PageKeyedDataSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.ibashkimi.wheel.core.skipAmount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FirestoreDataSource<T>(
    val query: Query,
    val mapper: suspend (QuerySnapshot) -> List<T>
) : PageKeyedDataSource<DocumentSnapshot, T>() {

    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        query.changes()
            .catch { }
            .skipAmount(1)
            .onEach {
                Log.d("FirestoreDataSource", "changed")
                invalidate()
            }
            .launchIn(scope)
    }

    override fun loadInitial(
        params: LoadInitialParams<DocumentSnapshot>,
        callback: LoadInitialCallback<DocumentSnapshot, T>
    ) {
        scope.launch {
            query.endAt(params.requestedLoadSize).asFlow().take(1).collect {
                callback.onResult(
                    mapper(it),
                    null, //it.documents.firstOrNull(),
                    if (it.documents.isEmpty()) null else it.documents[it.size() - 1]
                )
            }
        }
    }

    override fun loadAfter(
        params: LoadParams<DocumentSnapshot>,
        callback: LoadCallback<DocumentSnapshot, T>
    ) {
        scope.launch {
            query.startAfter(params.key).endAt(params.requestedLoadSize).asFlow().take(1).collect {
                val key = if (it.documents.isEmpty()) null else it.documents[it.size() - 1]
                callback.onResult(
                    mapper(it),
                    if (it.documents.size >= params.requestedLoadSize) key else null
                )
            }
        }
    }

    override fun loadBefore(
        params: LoadParams<DocumentSnapshot>,
        callback: LoadCallback<DocumentSnapshot, T>
    ) {
        // todo it's not correct
        scope.launch {
            query.startAfter(params.key).endAt(params.requestedLoadSize).asFlow().take(1).collect {
                callback.onResult(mapper(it), it.documents[it.size() - 1])
            }
        }
    }

    override fun invalidate() {
        super.invalidate()
        scope.cancel()
    }
}