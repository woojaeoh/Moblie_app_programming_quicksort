package com.example.quicksort.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TrashGuideRepository {
    private val db = FirebaseFirestore.getInstance()
    private val trashGuideCollection = db.collection("trash_guide")

    /**
     * AI 응답으로 받은 category와 detail로 가이드 검색 (Subcollection 구조)
     * "클래스 분류 불가"일 경우 일반쓰레기로 매칭
     * detail이 없으면 "기타" 문서로 매칭
     */
    suspend fun getGuide(category: String, detail: String): Result<List<String>> {
        return try {
            // 0. "클래스 분류 불가"인 경우 일반쓰레기로 리다이렉트
            val actualCategory = if (category == "클래스 분류 불가") {
                "일반쓰레기"
            } else {
                category
            }

            // 1. 카테고리 문서 존재 확인
            val categoryDoc = trashGuideCollection.document(actualCategory).get().await()
            if (!categoryDoc.exists()) {
                return Result.failure(Exception("카테고리를 찾을 수 없습니다: $actualCategory"))
            }

            // 2. details subcollection에서 detail 문서 조회
            val detailDoc = trashGuideCollection
                .document(actualCategory)
                .collection("details")
                .document(detail)
                .get()
                .await()

            if (detailDoc.exists()) {
                val description = detailDoc.get("description") as? List<String> ?: emptyList()
                return Result.success(description)
            }

            // 3. detail이 없으면 "기타" 문서로 fallback
            val etcDoc = trashGuideCollection
                .document(actualCategory)
                .collection("details")
                .document("기타")
                .get()
                .await()

            if (etcDoc.exists()) {
                val description = etcDoc.get("description") as? List<String> ?: emptyList()
                Result.success(description)
            } else {
                Result.failure(Exception("해당 detail에 대한 가이드가 없습니다: $detail"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 특정 카테고리의 모든 details 가져오기 (Subcollection 조회)
     */
    suspend fun getCategoryDetails(category: String): Result<Map<String, List<String>>> {
        return try {
            // 카테고리 문서 존재 확인
            val categoryDoc = trashGuideCollection.document(category).get().await()
            if (!categoryDoc.exists()) {
                return Result.failure(Exception("카테고리를 찾을 수 없습니다: $category"))
            }

            // details subcollection의 모든 문서 조회
            val detailsSnapshot = trashGuideCollection
                .document(category)
                .collection("details")
                .get()
                .await()

            val detailsMap = mutableMapOf<String, List<String>>()
            for (doc in detailsSnapshot.documents) {
                val description = doc.get("description") as? List<String> ?: emptyList()
                detailsMap[doc.id] = description
            }

            Result.success(detailsMap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
