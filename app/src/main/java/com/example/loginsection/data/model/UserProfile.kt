package com.example.loginsection.data.model

//개인 정보
//비밀번호는 firestore 가 아닌 firebaseauth에 저장
data class UserProfile(
    val uid: String = "",
    val id: String = "",
    val email: String = "",
    val points: Long = 0L
)

