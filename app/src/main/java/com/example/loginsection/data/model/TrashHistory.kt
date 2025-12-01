package com.example.loginsection.data.model

//UserProfile 아래에 들어갈 TrashHistory
data class TrashHistory(
    val id: String = "",
    val image_url: String = "",
    val category: String = "",
    val detail: String = "",
    val guide: String = "",
    val points_earned: Int = 0,
    val date: String = "",
)
