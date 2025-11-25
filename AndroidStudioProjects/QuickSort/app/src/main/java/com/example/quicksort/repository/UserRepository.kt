package com.example.quicksort.repository

import com.example.quicksort.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    /**
     * 사용자 정보 가져오기
     */
    suspend fun getUser(userId: String): Result<User> {
        return try {
            val document = usersCollection.document(userId).get().await()
            val user = document.toObject(User::class.java)

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
     * 랭킹 - 점수 순으로 사용자 목록 가져오기
     */
    suspend fun getRanking(limit: Int = 10): Result<List<User>> {
        return try {
            val snapshot = usersCollection
                .orderBy("points", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val users = snapshot.documents.mapNotNull {
                it.toObject(User::class.java)
            }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 사용자의 현재 점수 가져오기
     */
    suspend fun getUserPoints(userId: String): Result<Int> {
        return try {
            val document = usersCollection.document(userId).get().await()
            val points = document.getLong("points")?.toInt() ?: 100
            Result.success(points)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 특정 사용자의 랭킹 순위 가져오기
     */
    suspend fun getUserRank(userId: String): Result<Int> {
        return try {
            // 해당 사용자의 점수 가져오기
            val userPoints = getUserPoints(userId).getOrThrow()

            // 해당 점수보다 높은 사용자 수 세기
            val snapshot = usersCollection
                .whereGreaterThan("points", userPoints)
                .get()
                .await()

            val rank = snapshot.size() + 1  // 순위는 1부터 시작
            Result.success(rank)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
