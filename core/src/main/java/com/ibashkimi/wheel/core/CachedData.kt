package com.ibashkimi.wheel.core

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import java.lang.ref.WeakReference

class CachedData<T>(val getter: suspend (String) -> Flow<T?>) {

    private val map = HashMap<String, WeakReference<T?>>()

    suspend fun getItem(id: String): T? {
        val item = map[id]?.get()
        if (item == null) {
            val newItemDeferred = CompletableDeferred<T?>()
            getter(id).take(1).collect {
                newItemDeferred.complete(it)
            }
            val newItem = newItemDeferred.await()
            map[id] = WeakReference(newItem)
            return newItem
        }
        return item
    }
}