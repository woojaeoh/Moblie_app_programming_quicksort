package com.example.quicksort.repository

import com.example.quicksort.models.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthManager {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    /**
     * 현재 로그인된 사용자 가져오기
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    /**
     * 현재 로그인된 사용자의 UID 가져오기
     */
    fun getCurrentUid(): String? {
        return auth.currentUser?.uid
    }

    /**
     * 회원가입 (username, email, password)
     */
    suspend fun signUp(username: String, email: String, password: String): Result<UserProfile> {
        return try {
            // 1. FirebaseAuth 계정 생성
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("회원가입 실패"))

            // 2. Firestore에 UserProfile 생성
            val profile = UserProfile(
                uid = user.uid,
                username = username,
                email = email,
                totalCarbonReduced = 0.0
            )

            db.collection("users")
                .document(user.uid)
                .set(profile)
                .await()

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 로그인 (email, password)
     */
    suspend fun signIn(email: String, password: String): Result<UserProfile> {
        return try {
            // 1. FirebaseAuth 로그인
            auth.signInWithEmailAndPassword(email, password).await()
            val user = auth.currentUser ?: return Result.failure(Exception("로그인 실패"))

            // 2. Firestore에서 UserProfile 가져오기
            val docRef = db.collection("users").document(user.uid)
            val document = docRef.get().await()

            if (document.exists()) {
                val profile = document.toObject(UserProfile::class.java)
                    ?: return Result.failure(Exception("프로필을 찾을 수 없습니다"))
                Result.success(profile)
            } else {
                // 프로필이 없으면 새로 생성 (마이그레이션 케이스)
                val profile = UserProfile(
                    uid = user.uid,
                    username = email.substringBefore("@"),
                    email = email,
                    totalCarbonReduced = 0.0
                )
                docRef.set(profile).await()
                Result.success(profile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 로그아웃
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * 로그인 상태 확인
     */
    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}
