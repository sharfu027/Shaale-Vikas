package com.example.shaale_vikas

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Pledge(
    @DocumentId
    val firebaseId: String = "",
    val needId: String = "",
    val needTitle: String = "",
    val donorId: String = "", // Added for better tracking
    val donorName: String = "",
    val amount: Double = 0.0,
    val status: String = "PENDING", // PENDING, APPROVED
    @ServerTimestamp
    val timestamp: Date? = null
)
