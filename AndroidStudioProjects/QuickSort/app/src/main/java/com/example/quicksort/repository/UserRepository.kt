package com.example.quicksort.repository

import com.example.quicksort.models.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    /**
     * 신규 사용자 생성 (회원가입 시 호출)
     * Firebase Authentication 성공 후 Firestore에 UserProfile 생성
     */
    suspend fun createUser(uid: String, username: String, email: String): Result<Unit> {
        return try {
            val userProfile = UserProfile(
                uid = uid,
                username = username,
                email = email,
                totalCarbonReduced = 0.0
            )

            usersCollection.document(uid).set(userProfile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 사용자 정보 가져오기 (uid 기반)
     */
    suspend fun getUser(uid: String): Result<UserProfile> {
        return try {
            val document = usersCollection.document(uid).get().await()
            val user = document.toObject(UserProfile::class.java)

            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("사용자를 찾을 수 없습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 랭킹 - CO₂ 절감량 순으로 사용자 목록 가져오기
     */
    suspend fun getRanking(limit: Int = 10): Result<List<UserProfile>> {
        return try {
            val snapshot = usersCollection
                .orderBy("totalCarbonReduced", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val users = snapshot.documents.mapNotNull {
                it.toObject(UserProfile::class.java)
            }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 사용자의 현재 CO₂ 절감량 가져오기
     */
    suspend fun getUserCarbon(uid: String): Result<Double> {
        return try {
            val document = usersCollection.document(uid).get().await()
            val carbon = document.getDouble("totalCarbonReduced") ?: 0.0
            Result.success(carbon)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 특정 사용자의 랭킹 순위 가져오기
     */
    suspend fun getUserRank(uid: String): Result<Int> {
        return try {
            // 해당 사용자의 CO₂ 절감량 가져오기
            val userCarbon = getUserCarbon(uid).getOrThrow()

            // 해당 절감량보다 높은 사용자 수 세기
            val snapshot = usersCollection
                .whereGreaterThan("totalCarbonReduced", userCarbon)
                .get()
                .await()

            val rank = snapshot.size() + 1  // 순위는 1부터 시작
            Result.success(rank)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
