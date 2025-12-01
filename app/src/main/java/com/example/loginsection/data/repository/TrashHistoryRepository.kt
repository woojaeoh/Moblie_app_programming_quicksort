package com.example.loginsection.data.repository

import com.example.loginsection.data.model.TrashHistory
import com.example.loginsection.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

//보관된 Trash History 조회 및 새 Trash History 추가 기능
//데이터 아예 없는 경우 더미 데이터 제공(임시)
class TrashHistoryRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
) {

    // 현재 로그인한 유저 uid
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // 유저 프로필 한 번 로드
    fun loadUserProfile(
        onResult: (Result<UserProfile?>) -> Unit
    ) {
        val uid = getCurrentUserId()
        if (uid == null) {
            onResult(Result.failure(IllegalStateException("로그인 정보가 없습니다.")))
            return
        }

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val profile = doc.toObject(UserProfile::class.java)
                onResult(Result.success(profile))
            }
            .addOnFailureListener { e ->
                onResult(Result.failure(e))
            }
    }

    // 내 trash_history 실시간 구독
    fun listenMyTrashHistory(
        onChanged: (Result<List<TrashHistory>>) -> Unit
    ): ListenerRegistration? {
        val uid = getCurrentUserId()
        if (uid == null) {
            onChanged(Result.failure(IllegalStateException("로그인 정보가 없습니다.")))
            return null
        }

        val colRef = db.collection("users")
            .document(uid)
            .collection("trash_history")
            .orderBy("date", Query.Direction.DESCENDING)

        return colRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                onChanged(Result.failure(e))
                return@addSnapshotListener
            }

            if (snapshot == null) {
                onChanged(Result.success(emptyList()))
                return@addSnapshotListener
            }

            val list = snapshot.documents.mapNotNull { doc ->
                val history = doc.toObject(TrashHistory::class.java)
                history?.copy(id = doc.id) // id 필드 쓰는 경우
            }

            onChanged(Result.success(list))
        }
    }

    // TrashHistory 한 건 추가
    fun addTrashHistory(history: TrashHistory, onComplete: (Result<Unit>) -> Unit) {
        val uid = getCurrentUserId()
        if (uid == null) {
            onComplete(Result.failure(IllegalStateException("로그인 정보가 없습니다.")))
            return
        }

        val colRef = db.collection("users")
            .document(uid)
            .collection("trash_history")

        val docRef = colRef.document()
        val dataToSave = history.copy(id = docRef.id)

        docRef.set(dataToSave)
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }

    fun addDummyHistories(onComplete: () -> Unit) {
        val uid = getCurrentUserId()
        if (uid == null) {
            onComplete()
            return
        }

        val dummyList = listOf(
            TrashHistory(
                image_url = "https://example.com/dummy1.jpg",
                category = "플라스틱",
                detail = "생수병",
                guide = "라벨 제거 후 배출",
                points_earned = 10,
                date = "2025-11-30"
            ),
            TrashHistory(
                image_url = "https://example.com/dummy2.jpg",
                category = "고철류",
                detail = "프라이팬",
                guide = "손잡이 분리 후 배출",
                points_earned = 20,
                date = "2025-11-29"
            )
        )

        val colRef = db.collection("users")
            .document(uid)
            .collection("trash_history")

        var completed = 0
        val total = dummyList.size

        dummyList.forEach { item ->
            colRef.add(item)
                .addOnCompleteListener {
                    completed++
                    if (completed == total) {
                        onComplete()
                    }
                }
        }
    }
}