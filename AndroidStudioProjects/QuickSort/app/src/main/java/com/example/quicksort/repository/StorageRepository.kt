package com.example.quicksort.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StorageRepository {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

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
            // 파일명: users/{userId}/images/{UUID}.jpg
            val fileName = "${UUID.randomUUID()}.jpg"
            val imageRef = storageRef.child("users/$userId/images/$fileName")

            // 업로드
            val uploadTask = imageRef.putFile(imageUri).await()

            // 다운로드 URL 가져오기
            val downloadUrl = imageRef.downloadUrl.await().toString()

            // 업로드 성공 후 에뮬레이터 임시 파일 삭제
            deleteLocalFile(imageUri, context)

            Result.success(downloadUrl)
        } catch (e: Exception) {
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
