package com.example.shaale_vikas

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Need(
    @DocumentId
    val firebaseId: String = "", // Unique Firestore ID to prevent duplicates
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val estimatedCost: Double = 0.0,
    var currentAmount: Double = 0.0,
    val imageUrl: String = "",
    var status: String = "OPEN",
    val beforeImageUrl: String = "",
    var afterImageUrl: String = ""
) {
    fun getProgress(): Int {
        if (estimatedCost <= 0) return 0
        val progress = ((currentAmount / estimatedCost) * 100).toInt()
        return progress.coerceAtMost(100)
    }
}
