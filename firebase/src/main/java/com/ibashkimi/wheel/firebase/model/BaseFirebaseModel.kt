package com.ibashkimi.wheel.firebase.model

import com.google.firebase.database.Exclude

abstract class BaseFirebaseModel(@Exclude open var uid: String)