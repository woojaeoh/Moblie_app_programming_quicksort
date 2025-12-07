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
     * users/{uid}/trash_history subcollection에 저장
     */
    suspend fun addTrashHistory(
        uid: String,
        imageUrl: String,
        category: String,
        detail: String,
        guide: List<String>,
        carbonReduced: Double
    ): Result<String> {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate = dateFormat.format(Date())

            // 자동 생성될 ID를 미리 가져오기
            val docRef = db.collection("users")
                .document(uid)
                .collection("trash_history")
                .document()

            val history = TrashHistory(
                id = docRef.id,
                image_url = imageUrl,
                category = category,
                detail = detail,
                guide = guide,  // List<String> 그대로 저장
                carbonReduced = carbonReduced,
                date = currentDate
            )

            // Firestore에 저장
            docRef.set(history).await()

            // 사용자 CO₂ 절감량 업데이트
            updateUserCarbon(uid, carbonReduced)

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 사용자의 trash_history 가져오기 (최신순)
     */
    suspend fun getUserTrashHistory(uid: String): Result<List<TrashHistory>> {
        return try {
            val snapshot = db.collection("users")
                .document(uid)
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
     * 사용자 CO₂ 절감량 업데이트 (트랜잭션으로 안전하게)
     */
    private suspend fun updateUserCarbon(uid: String, carbonToAdd: Double) {
        try {
            val userRef = db.collection("users").document(uid)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentCarbon = snapshot.getDouble("totalCarbonReduced") ?: 0.0
                transaction.update(userRef, "totalCarbonReduced", currentCarbon + carbonToAdd)
            }.await()
        } catch (e: Exception) {
            // 에러 로깅만 하고 메인 작업은 성공으로 처리
            android.util.Log.e("TrashHistoryRepo", "Failed to update carbon", e)
        }
    }

    /**
     * 특정 기록 삭제 (totalCarbonReduced도 함께 감소)
     */
    suspend fun deleteTrashHistory(uid: String, historyId: String): Result<Unit> {
        return try {
            // 1. 삭제 전에 carbonReduced 값 가져오기
            val historyRef = db.collection("users")
                .document(uid)
                .collection("trash_history")
                .document(historyId)

            val snapshot = historyRef.get().await()
            val carbonReduced = snapshot.getDouble("carbonReduced") ?: 0.0

            // 2. 기록 삭제
            historyRef.delete().await()

            // 3. totalCarbonReduced 감소
            decreaseUserCarbon(uid, carbonReduced)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 사용자 CO₂ 절감량 감소 (기록 삭제 시)
     */
    private suspend fun decreaseUserCarbon(uid: String, carbonToSubtract: Double) {
        try {
            val userRef = db.collection("users").document(uid)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentCarbon = snapshot.getDouble("totalCarbonReduced") ?: 0.0
                val newCarbon = (currentCarbon - carbonToSubtract).coerceAtLeast(0.0) // 음수 방지
                transaction.update(userRef, "totalCarbonReduced", newCarbon)
            }.await()
        } catch (e: Exception) {
            // 에러 로깅만 하고 메인 작업은 성공으로 처리
            android.util.Log.e("TrashHistoryRepo", "Failed to decrease carbon", e)
        }
    }
}

