package com.example.quicksort.repository

import com.example.quicksort.models.TrashHistory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrashHistoryRepository {
    private val db = FirebaseFirestore.getInstance()

    /**
     * 사용자의 trash_history에 새 기록 추가
     * users/{userId}/trash_history subcollection에 저장
     */
    suspend fun addTrashHistory(
        userId: String,
        imageUrl: String,
        category: String,
        detail: String,
        guide: List<String>,
        pointsEarned: Int
    ): Result<String> {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate = dateFormat.format(Date())

            // guide List<String>을 하나의 문자열로 결합
            val guideText = guide.joinToString("\n")

            val history = TrashHistory(
                image_url = imageUrl,
                category = category,
                detail = detail,
                guide = guideText,
                points_earned = pointsEarned,
                date = currentDate
            )

            // users/{userId}/trash_history subcollection에 추가
            val docRef = db.collection("users")
                .document(userId)
                .collection("trash_history")
                .add(history)
                .await()

            // 사용자 points 업데이트
            updateUserPoints(userId, pointsEarned)

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 사용자의 trash_history 가져오기 (최신순)
     */
    suspend fun getUserTrashHistory(userId: String): Result<List<TrashHistory>> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("trash_history")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            val histories = snapshot.documents.mapNotNull {
                it.toObject(TrashHistory::class.java)
            }

            Result.success(histories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 사용자 점수 업데이트 (트랜잭션으로 안전하게)
     */
    private suspend fun updateUserPoints(userId: String, pointsToAdd: Int) {
        try {
            val userRef = db.collection("users").document(userId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentPoints = snapshot.getLong("points")?.toInt() ?: 100
                transaction.update(userRef, "points", currentPoints + pointsToAdd)
            }.await()
        } catch (e: Exception) {
            // 에러 로깅만 하고 메인 작업은 성공으로 처리
            android.util.Log.e("TrashHistoryRepo", "Failed to update points", e)
        }
    }

    /**
     * 특정 기록 삭제
     */
    suspend fun deleteTrashHistory(userId: String, historyId: String): Result<Unit> {
        return try {
            db.collection("users")
                .document(userId)
                .collection("trash_history")
                .document(historyId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
