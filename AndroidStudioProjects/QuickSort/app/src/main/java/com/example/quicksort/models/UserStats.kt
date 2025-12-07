package com.example.quicksort.models

data class UserStats(
    val total_points: Int = 0,
    val category_counts: Map<String, Int> = emptyMap(),
    val last_updated: String = ""
)
