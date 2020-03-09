package com.ibashkimi.wheel.firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

open class BaseFirestoreManager {

    protected val db = FirebaseFirestore.getInstance()

    init {
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
    }

    protected val currentUser: FirebaseUser?
        get() = FirebaseAuth.getInstance().currentUser
}