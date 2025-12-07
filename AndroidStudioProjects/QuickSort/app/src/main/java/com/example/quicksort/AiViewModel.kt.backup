package com.example.quicksort

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Log
import com.example.quicksort.models.TrashHistory
import com.example.quicksort.models.UserProfile
import com.example.quicksort.repository.StorageRepository
import com.example.quicksort.repository.TrashGuideRepository
import com.example.quicksort.repository.TrashHistoryRepository
import com.example.quicksort.repository.UserRepository
import com.example.quicksort.utils.CarbonCalculator
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

    // 분석 결과 StateFlow (ResultScreen용)
    data class AnalysisResult(
        val imageUri: Uri,
        val category: String,
        val detail: String
    )

    private val _analysisResult = MutableStateFlow<AnalysisResult?>(null)
    val analysisResult: StateFlow<AnalysisResult?> = _analysisResult.asStateFlow()

    // 분석 결과를 담는 데이터 클래스
    data class RecyclingResult(
        val imageUrl: String,
        val category: String,
        val detail: String,
        val guide: List<String>,
        val carbonReduced: Double  // kg CO₂eq
    )

    /**
     * 1단계: 사진 분석 및 법령 조회 (화면 표시용)
     * Storage 업로드 → AI 분석 → 가이드 검색
     * 업로드 성공 시 에뮬레이터의 임시 파일 자동 삭제
     *
     * @param uid 사용자 UID (Storage 폴더 구분용)
     * @param imageUri 촬영한 이미지의 로컬 URI
     * @param context Context (임시 파일 삭제용)
     * @param onResult 결과 콜백 (category, detail, guide, carbonReduced)
     */
    fun analyzeRecycling(uid: String, imageUri: Uri, context: Context, onResult: (RecyclingResult?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // 1. Firebase Storage에 이미지 업로드 (업로드 후 자동으로 로컬 임시 파일 삭제)
                Log.d("RECYCLING", "이미지 업로드 중...")
                val uploadResult = storageRepo.uploadImage(imageUri, uid, context)

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

                // 4. 카테고리별 CO₂ 절감량 계산
                val carbonReduced = CarbonCalculator.calculateCarbon(category)

                // 결과 반환 (저장은 하지 않음)
                val recyclingResult = RecyclingResult(
                    imageUrl = imageUrl,
                    category = category,
                    detail = detail,
                    guide = guide,
                    carbonReduced = carbonReduced
                )

                Log.d("RECYCLING", "분석 완료 - ${carbonReduced}kg CO₂ 절감 예상")
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
     * @param uid 사용자 UID
     * @param result analyzeRecycling()에서 받은 결과
     */
    fun saveToHistory(uid: String, result: RecyclingResult, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val saveResult = trashHistoryRepo.addTrashHistory(
                    uid = uid,
                    imageUrl = result.imageUrl,
                    category = result.category,
                    detail = result.detail,
                    guide = result.guide,
                    carbonReduced = result.carbonReduced
                )

                if (saveResult.isFailure) {
                    _errorMessage.value = "저장 실패: ${saveResult.exceptionOrNull()?.message}"
                    return@launch
                }

                Log.d("RECYCLING", "저장 완료 - ${result.carbonReduced}kg CO₂ 절감!")
                onSuccess()

            } catch (e: Exception) {
                Log.e("RECYCLING", "저장 실패", e)
                _errorMessage.value = "저장 실패: ${e.message}"
            }
        }
    }

    /**
     * 사용자가 저장 안 할 경우 업로드한 이미지 삭제 (저장 안함 버튼)
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
     * 사용자의 분리수거 기록 가져오기 (갤러리용) -> 프론트에서 coil라이브러리로 인스타그램처럼 사진 3분할로 보여주면 좋을듯.
     */
    fun getUserHistory(uid: String, onResult: (List<TrashHistory>) -> Unit) {
        viewModelScope.launch {
            val result = trashHistoryRepo.getUserTrashHistory(uid)
            result.onSuccess { histories ->
                onResult(histories)
            }.onFailure { e ->
                Log.e("RECYCLING", "기록 조회 실패", e)
                _errorMessage.value = "기록 조회 실패: ${e.message}"
            }
        }
    }

    /**
     * 랭킹 가져오기 (CO₂ 절감량으로 정렬한 결과 반환)
     */
    fun getRanking(limit: Int = 10, onResult: (List<UserProfile>) -> Unit) {
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
     * 사용자 정보 가져오기 -> 개인정보 프로필 화면이 있다면 사용. ex) 로그인 후 홈 화면
     */
    fun getUserInfo(uid: String, onResult: (UserProfile) -> Unit) {
        viewModelScope.launch {
            val result = userRepo.getUser(uid)
            result.onSuccess { user ->
                onResult(user)
            }.onFailure { e ->
                Log.e("RECYCLING", "사용자 조회 실패", e)
                _errorMessage.value = "사용자 조회 실패: ${e.message}"
            }
        }
    }

    /**
     * 내 순위 가져오기 -> 프로필 화면에서 내 순위 표시. , 전체 랭킹 화면에서 내 순위 강조 표시
     */
    fun getMyRank(uid: String, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val result = userRepo.getUserRank(uid)
            result.onSuccess { rank ->
                onResult(rank)
            }.onFailure { e ->
                Log.e("RECYCLING", "순위 조회 실패", e)
                _errorMessage.value = "순위 조회 실패: ${e.message}"
            }
        }
    }

    /**
     * 기록 삭제 (이미지도 함께 삭제) -> 내가 저장한 기록중에 갤러리에서 사진 삭제하듯이 , 저장사진 삭제.
     */
    fun deleteHistory(uid: String, historyId: String, imageUrl: String) {
        viewModelScope.launch {
            try {
                // Storage에서 이미지 삭제
                storageRepo.deleteImage(imageUrl)

                // Firestore에서 기록 삭제
                trashHistoryRepo.deleteTrashHistory(uid, historyId)

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

    // ===== ResultScreen용 헬퍼 함수들 =====

    /**
     * 간단한 analyzeRecycling 오버로드 (ResultScreen에서 사용)
     */
    fun analyzeRecycling(imageUri: Uri, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // AI 분석 (간단한 버전 - Storage 업로드 없이 로컬 분석)
                // 실제로는 AI 서버에 전송해야 하지만, 여기서는 analysisResult만 설정
                _analysisResult.value = AnalysisResult(
                    imageUri = imageUri,
                    category = "플라스틱",
                    detail = "페트병"
                )
                onComplete(true)
            } catch (e: Exception) {
                Log.e("AiViewModel", "분석 실패", e)
                onComplete(false)
            }
        }
    }

    /**
     * 카테고리별 가이드 가져오기 (ResultScreen용 - List<String> 반환)
     */
    fun getGuideForCategory(category: String, onResult: (List<String>) -> Unit) {
        viewModelScope.launch {
            try {
                val guideResult = trashGuideRepo.getGuide(category,"")

                if (guideResult.isSuccess) {
                    val descriptions = guideResult.getOrNull() ?: emptyList()
                    if (descriptions.isNotEmpty()) {
                        onResult(descriptions)
                    } else {
                        onResult(listOf("가이드를 찾을 수 없습니다."))
                    }
                } else {
                    onResult(listOf("가이드를 찾을 수 없습니다."))
                }
            } catch (e: Exception) {
                Log.e("AiViewModel", "가이드 가져오기 실패", e)
                onResult(listOf("가이드 로드 중 오류가 발생했습니다."))
            }
        }
    }

    /**
     * 카테고리별 CO2 절감량 계산
     */
    fun calculateCarbonReduction(category: String): Double {
        return CarbonCalculator.calculateCarbon(category)
    }

    /**
     * 분리수거 기록 저장 (ResultScreen용 오버로드)
     */
    fun saveToHistory(
        uid: String,
        imageUrl: String,
        category: String,
        detail: String,
        guide: String,
        carbonReduced: Double,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = trashHistoryRepo.addTrashHistory(
                    uid = uid,
                    imageUrl = imageUrl,
                    category = category,
                    detail = detail,
                    guide = guide.split("\n"),  // String을 List<String>으로 변환
                    carbonReduced = carbonReduced
                )
                onComplete(result.isSuccess)
            } catch (e: Exception) {
                Log.e("AiViewModel", "기록 저장 실패", e)
                onComplete(false)
            }
        }
    }
}
