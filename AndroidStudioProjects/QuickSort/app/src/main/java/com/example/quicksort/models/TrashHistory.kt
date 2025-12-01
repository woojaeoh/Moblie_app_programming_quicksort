package com.example.quicksort.models

data class TrashHistory(
    val id: String = "",
    val image_url: String = "",
    val category: String = "",
    val detail: String = "",
    val guide: List<String> = emptyList(),  // 가이드 배열
    val carbonReduced: Double = 0.0,  // kg CO₂eq
    val date: String = ""
)
