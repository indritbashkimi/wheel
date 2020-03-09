package com.ibashkimi.wheel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ibashkimi.wheel.core.asScopedLiveData
import com.ibashkimi.wheel.firestore.core.FirestoreUserManager

class EditProfileViewModel(userId: String) : ViewModel() {

    private val repository = FirestoreUserManager()

    val user = repository.getUser(userId).asScopedLiveData(viewModelScope)

    class Factory(private val userId: String) :
        ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return EditProfileViewModel(userId) as T
        }
    }
}