package com.ibashkimi.wheel.core

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

fun <T> Flow<T>.asScopedLiveData(scope: CoroutineScope): LiveData<T> {
    val liveData = MutableLiveData<T>()
    this
        .onEach { liveData.value = it }
        .catch { liveData.value = null }
        .launchIn(scope)
    return liveData
}

fun <T> Flow<T>.skipAmount(amount: Int): Flow<T> = flow {
    var skipped = 0
    collect {
        if (skipped == amount) {
            emit(it)
        } else {
            skipped += 1
        }
    }
}