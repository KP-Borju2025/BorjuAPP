package com.kp.borju_kp.data

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class User(
    @get:Exclude var uid: String = "",

    // Gunakan @PropertyName untuk memetakan field Firestore ke properti Kotlin
    @get:PropertyName("nama_user") @set:PropertyName("nama_user")
    var name: String = "",

    @get:PropertyName("email") @set:PropertyName("email")
    var email: String = "",

    @get:PropertyName("nama_role") @set:PropertyName("nama_role")
    var role: String = ""
)