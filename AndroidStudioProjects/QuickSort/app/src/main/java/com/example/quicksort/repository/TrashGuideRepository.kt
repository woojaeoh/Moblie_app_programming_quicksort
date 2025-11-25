package com.example.quicksort.repository

import com.example.quicksort.models.TrashGuide
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TrashGuideRepository {
    private val db = FirebaseFirestore.getInstance()
    private val trashGuideCollection = db.collection("trash_guide")

    /**
     * AI 응답으로 받은 category와 detail로 가이드 검색
     * detail이 없으면 "기타" 필드로 매칭
     */
    suspend fun getGuide(category: String, detail: String): Result<List<String>> {
        return try {
            val document = trashGuideCollection.document(category).get().await()

            if (!document.exists()) {
                return Result.failure(Exception("카테고리를 찾을 수 없습니다: $category"))
            }

            val trashGuide = document.toObject(TrashGuide::class.java)
            val details = trashGuide?.details ?: emptyMap()

            // 1. detail이 정확히 매칭되는지 확인
            val description = details[detail]?.description
                ?: details["기타"]?.description  // 2. 없으면 "기타"로 매칭
                ?: emptyList()  // 3. "기타"도 없으면 빈 리스트

            if (description.isEmpty()) {
                Result.failure(Exception("해당 detail에 대한 가이드가 없습니다: $detail"))
            } else {
                Result.success(description)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 특정 카테고리의 모든 details 가져오기
     */
    suspend fun getCategoryDetails(category: String): Result<Map<String, List<String>>> {
        return try {
            val document = trashGuideCollection.document(category).get().await()

            if (!document.exists()) {
                return Result.failure(Exception("카테고리를 찾을 수 없습니다: $category"))
            }

            val trashGuide = document.toObject(TrashGuide::class.java)
            val detailsMap = trashGuide?.details?.mapValues { it.value.description } ?: emptyMap()

            Result.success(detailsMap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
