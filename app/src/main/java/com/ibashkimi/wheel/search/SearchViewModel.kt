package com.ibashkimi.wheel.search

import androidx.lifecycle.ViewModel
import androidx.paging.toLiveData
import com.ibashkimi.wheel.firestore.core.FirestoreUserManager

class SearchViewModel : ViewModel() {
    private val repository = FirestoreUserManager()

    val users = repository.getAllUsersPaged().toLiveData(20)
}