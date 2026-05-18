package com.example.shaale_vikas

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "ALUMNI" // Roles: ADMIN, ALUMNI
)
