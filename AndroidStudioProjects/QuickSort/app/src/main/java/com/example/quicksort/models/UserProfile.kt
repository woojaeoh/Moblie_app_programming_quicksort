package com.example.quicksort.models

data class UserProfile(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val totalCarbonReduced: Double = 0.0  // kg CO₂eq
)
