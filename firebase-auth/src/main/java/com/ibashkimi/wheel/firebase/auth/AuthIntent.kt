package com.ibashkimi.wheel.firebase.auth

import android.content.Context
import android.content.Intent
import com.firebase.ui.auth.AuthUI

fun getFirebaseAuthIntent(): Intent {
    return AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(
            listOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build(),
                AuthUI.IdpConfig.AnonymousBuilder().build(),
                AuthUI.IdpConfig.PhoneBuilder().build()
            )
        )
        .build()
}

fun firebaseLogOut(context: Context) {
    AuthUI.getInstance().signOut(context)
}