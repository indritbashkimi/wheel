package com.ibashkimi.wheel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ibashkimi.wheel.firestore.core.FirestoreUserManager

class AuthViewModel : ViewModel() {
    val userLiveData = MutableLiveData<String?>()

    init {
        userLiveData.value = FirestoreUserManager().currentUserId
    }
}