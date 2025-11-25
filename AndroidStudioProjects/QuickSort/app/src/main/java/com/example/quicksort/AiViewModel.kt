package com.example.quicksort

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Log
import com.example.quicksort.models.TrashHistory
import com.example.quicksort.models.User
import com.example.quicksort.repository.StorageRepository
import com.example.quicksort.repository.TrashGuideRepository
import com.example.quicksort.repository.TrashHistoryRepository
import com.example.quicksort.repository.UserRepository
import com.example.quicksort.utils.PointsCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AiViewModel : ViewModel() {

    private val storageRepo = StorageRepository()
    private val trashGuideRepo = TrashGuideRepository()
    private val trashHistoryRepo = TrashHistoryRepository()
    private val userRepo = UserRepository()

    // 상태 관리 (프론트엔드에서 관찰 가능)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // 분석 결과를 담는 데이터 클래스
    data class RecyclingResult(
        val imageUrl: String,
        val category: String,
        val detail: String,
        val guide: List<String>,
        val points: Int
    )

    /**
     * 1단계: 사진 분석 및 법령 조회 (화면 표시용)
     * Storage 업로드 → AI 분석 → 가이드 검색
     *
     * @param userId 사용자 ID (Storage 폴더 구분용)
     * @param imageUri 촬영한 이미지의 로컬 URI
     * @param onResult 결과 콜백 (category, detail, guide, points)
     */
    fun analyzeRecycling(userId: String, imageUri: Uri, onResult: (RecyclingResult?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // 1. Firebase Storage에 이미지 업로드
                Log.d("RECYCLING", "이미지 업로드 중...")
                val uploadResult = storageRepo.uploadImage(imageUri, userId)

                if (uploadResult.isFailure) {
                    _errorMessage.value = "이미지 업로드 실패: ${uploadResult.exceptionOrNull()?.message}"
                    onResult(null)
                    return@launch
                }

                val imageUrl = uploadResult.getOrNull()!!
                Log.d("RECYCLING", "업로드 완료: $imageUrl")

                // 2. AI 서버로 이미지 분석 요청
                val aiRequest = AiRequest(image_url = imageUrl)
                val aiResponse = RetrofitClient.api.predict(aiRequest)

                if (!aiResponse.isSuccessful || aiResponse.body() == null) {
                    _errorMessage.value = "AI 분석 실패: ${aiResponse.code()}"
                    // 실패 시 업로드한 이미지 삭제
                    storageRepo.deleteImage(imageUrl)
                    onResult(null)
                    return@launch
                }

                val result = aiResponse.body()!!
                val category = result.category
                val detail = result.detail

                Log.d("RECYCLING", "AI 응답 - category: $category, detail: $detail")

                // 3. Firestore에서 가이드 검색 (detail 매칭 안되면 "기타")
                val guideResult = trashGuideRepo.getGuide(category, detail)

                if (guideResult.isFailure) {
                    _errorMessage.value = "가이드 검색 실패: ${guideResult.exceptionOrNull()?.message}"
                    // 실패 시 업로드한 이미지 삭제
                    storageRepo.deleteImage(imageUrl)
                    onResult(null)
                    return@launch
                }

                val guide = guideResult.getOrNull() ?: emptyList()

                // 4. 카테고리별 점수 계산
                val points = PointsCalculator.getPoints(category)

                // 결과 반환 (저장은 하지 않음)
                val recyclingResult = RecyclingResult(
                    imageUrl = imageUrl,
                    category = category,
                    detail = detail,
                    guide = guide,
                    points = points
                )

                Log.d("RECYCLING", "분석 완료 - $points 점 예상")
                onResult(recyclingResult)

            } catch (e: Exception) {
                Log.e("RECYCLING", "분석 실패", e)
                _errorMessage.value = "분석 실패: ${e.message}"
                onResult(null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 2단계: 분석 결과를 히스토리에 저장 (사용자가 저장 버튼 누를 때)
     *
     * @param userId 사용자 ID
     * @param result analyzeRecycling()에서 받은 결과
     */
    fun saveToHistory(userId: String, result: RecyclingResult, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val saveResult = trashHistoryRepo.addTrashHistory(
                    userId = userId,
                    imageUrl = result.imageUrl,
                    category = result.category,
                    detail = result.detail,
                    guide = result.guide,
                    pointsEarned = result.points
                )

                if (saveResult.isFailure) {
                    _errorMessage.value = "저장 실패: ${saveResult.exceptionOrNull()?.message}"
                    return@launch
                }

                Log.d("RECYCLING", "저장 완료 - ${result.points}점 획득!")
                onSuccess()

            } catch (e: Exception) {
                Log.e("RECYCLING", "저장 실패", e)
                _errorMessage.value = "저장 실패: ${e.message}"
            }
        }
    }

    /**
     * 사용자가 저장 안 할 경우 업로드한 이미지 삭제
     */
    fun cancelAndDeleteImage(imageUrl: String) {
        viewModelScope.launch {
            try {
                storageRepo.deleteImage(imageUrl)
                Log.d("RECYCLING", "이미지 삭제 완료")
            } catch (e: Exception) {
                Log.e("RECYCLING", "이미지 삭제 실패", e)
            }
        }
    }

    /**
     * 사용자의 분리수거 기록 가져오기 (갤러리용)
     */
    fun getUserHistory(userId: String, onResult: (List<TrashHistory>) -> Unit) {
        viewModelScope.launch {
            val result = trashHistoryRepo.getUserTrashHistory(userId)
            result.onSuccess { histories ->
                onResult(histories)
            }.onFailure { e ->
                Log.e("RECYCLING", "기록 조회 실패", e)
                _errorMessage.value = "기록 조회 실패: ${e.message}"
            }
        }
    }

    /**
     * 랭킹 가져오기
     */
    fun getRanking(limit: Int = 10, onResult: (List<User>) -> Unit) {
        viewModelScope.launch {
            val result = userRepo.getRanking(limit)
            result.onSuccess { users ->
                onResult(users)
            }.onFailure { e ->
                Log.e("RECYCLING", "랭킹 조회 실패", e)
                _errorMessage.value = "랭킹 조회 실패: ${e.message}"
            }
        }
    }

    /**
     * 사용자 정보 가져오기
     */
    fun getUserInfo(userId: String, onResult: (User) -> Unit) {
        viewModelScope.launch {
            val result = userRepo.getUser(userId)
            result.onSuccess { user ->
                onResult(user)
            }.onFailure { e ->
                Log.e("RECYCLING", "사용자 조회 실패", e)
                _errorMessage.value = "사용자 조회 실패: ${e.message}"
            }
        }
    }

    /**
     * 내 순위 가져오기
     */
    fun getMyRank(userId: String, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val result = userRepo.getUserRank(userId)
            result.onSuccess { rank ->
                onResult(rank)
            }.onFailure { e ->
                Log.e("RECYCLING", "순위 조회 실패", e)
                _errorMessage.value = "순위 조회 실패: ${e.message}"
            }
        }
    }

    /**
     * 기록 삭제 (이미지도 함께 삭제)
     */
    fun deleteHistory(userId: String, historyId: String, imageUrl: String) {
        viewModelScope.launch {
            try {
                // Storage에서 이미지 삭제
                storageRepo.deleteImage(imageUrl)

                // Firestore에서 기록 삭제
                trashHistoryRepo.deleteTrashHistory(userId, historyId)

                Log.d("RECYCLING", "기록 삭제 완료")
            } catch (e: Exception) {
                Log.e("RECYCLING", "삭제 실패", e)
                _errorMessage.value = "삭제 실패: ${e.message}"
            }
        }
    }

    // ========== 테스트 함수들 ==========
    fun testPing() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.ping()
                Log.d("AI_TEST", "ping 응답: ${response.body()}")
            } catch (e: Exception) {
                Log.e("AI_TEST", "ping 실패", e)
            }
        }
    }

    fun testPredict() {
        viewModelScope.launch {
            try {
                val req = AiRequest(image_url = "https://example.com/dummy.jpg")
                val response = RetrofitClient.api.predict(req)
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("AI_TEST", "predict 응답: $body")
                } else {
                    Log.e("AI_TEST", "predict 실패 코드: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("AI_TEST", "predict 예외 발생", e)
            }
        }
    }
}
