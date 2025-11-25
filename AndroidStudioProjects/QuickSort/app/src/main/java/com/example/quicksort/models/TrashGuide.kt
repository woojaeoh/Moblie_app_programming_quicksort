package com.example.quicksort.models

data class TrashGuide(
    val details: Map<String, DetailInfo> = emptyMap()
)

data class DetailInfo(
    val description: List<String> = emptyList()
)
