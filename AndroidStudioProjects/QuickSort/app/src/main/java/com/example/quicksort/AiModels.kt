package com.example.quicksort

data class AiRequest(
    val image_url: String
)

data class AiResponse(
    val status: String,
    val category: String,
    val detail: String
)
