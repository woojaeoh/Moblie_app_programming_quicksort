package com.example.quicksort.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StorageRepository {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference
    private val auth = FirebaseAuth.getInstance()

    /**
     * 이미지를 Firebase Storage에 업로드하고 다운로드 URL 반환
     * 업로드 성공 시 에뮬레이터의 임시 파일 자동 삭제
     *
     * @param imageUri 로컬 이미지 URI (카메라/갤러리에서 가져온)
     * @param userId 사용자 ID (폴더 구분용)
     * @param context Context (임시 파일 삭제용)
     * @return 업로드된 이미지의 다운로드 URL
     */
    suspend fun uploadImage(imageUri: Uri, userId: String, context: Context): Result<String> {
        return try {
            // 사용자 인증 상태 확인
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e("StorageRepo", "업로드 실패: 사용자가 로그인되어 있지 않습니다")
                return Result.failure(Exception("사용자가 로그인되어 있지 않습니다"))
            }

            Log.d("StorageRepo", "업로드 시작 - User: ${currentUser.uid}, Email: ${currentUser.email}")
            Log.d("StorageRepo", "이미지 URI: $imageUri")

            // 인증 토큰 새로고침 (만료된 토큰 문제 방지)
            try {
                val tokenResult = currentUser.getIdToken(true).await()
                Log.d("StorageRepo", "인증 토큰 갱신 성공")
            } catch (e: Exception) {
                Log.e("StorageRepo", "인증 토큰 갱신 실패", e)
                return Result.failure(Exception("인증 토큰 갱신 실패: ${e.message}"))
            }

            // 파일명: users/{userId}/images/{UUID}.jpg
            val fileName = "${UUID.randomUUID()}.jpg"
            val imageRef = storageRef.child("users/$userId/images/$fileName")

            Log.d("StorageRepo", "업로드 경로: users/$userId/images/$fileName")

            // 업로드
            val uploadTask = imageRef.putFile(imageUri).await()
            Log.d("StorageRepo", "업로드 완료: ${uploadTask.metadata?.path}")

            // 다운로드 URL 가져오기
            val downloadUrl = imageRef.downloadUrl.await().toString()
            Log.d("StorageRepo", "다운로드 URL: $downloadUrl")

            // 업로드 성공 후 에뮬레이터 임시 파일 삭제
            deleteLocalFile(imageUri, context)

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e("StorageRepo", "업로드 실패 - 에러 타입: ${e.javaClass.simpleName}", e)
            Log.e("StorageRepo", "에러 메시지: ${e.message}")
            Log.e("StorageRepo", "전체 스택 트레이스:", e)
            Result.failure(e)
        }
    }

    /**
     * 에뮬레이터/기기의 로컬 임시 파일 삭제
     */
    private fun deleteLocalFile(imageUri: Uri, context: Context) {
        try {
            context.contentResolver.delete(imageUri, null, null)
            Log.d("StorageRepo", "로컬 임시 파일 삭제 완료: $imageUri")
        } catch (e: Exception) {
            // 실패해도 메인 작업(업로드)에는 영향 없음
            Log.w("StorageRepo", "로컬 파일 삭제 실패 (무시 가능)", e)
        }
    }

    /**
     * 이미지 삭제
     * @param imageUrl Firebase Storage URL
     */
    suspend fun deleteImage(imageUrl: String): Result<Unit> {
        return try {
            val imageRef = storage.getReferenceFromUrl(imageUrl)
            imageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 사용자의 모든 이미지 삭제 (탈퇴 시)
     */
    suspend fun deleteAllUserImages(userId: String): Result<Unit> {
        return try {
            val userImagesRef = storageRef.child("users/$userId/images")
            val listResult = userImagesRef.listAll().await()

            // 모든 이미지 삭제
            listResult.items.forEach { item ->
                item.delete().await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
