package com.example.quicksort

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quicksort.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val userRepo = UserRepository()

    // 상태 관리
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    /**
     * 회원가입
     * Firebase Authentication + Firestore UserProfile 생성
     */
    fun signUp(email: String, password: String, username: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // 1. Firebase Authentication 회원가입
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = authResult.user?.uid

                if (uid == null) {
                    _errorMessage.value = "회원가입 실패: UID를 가져올 수 없습니다"
                    return@launch
                }

                Log.d("AUTH", "Firebase Authentication 회원가입 성공: $uid")

                // 2. Firestore에 UserProfile 생성
                val createResult = userRepo.createUser(uid, username, email)

                if (createResult.isFailure) {
                    _errorMessage.value = "사용자 정보 저장 실패: ${createResult.exceptionOrNull()?.message}"
                    // 실패 시 Authentication 계정도 삭제
                    authResult.user?.delete()?.await()
                    return@launch
                }

                Log.d("AUTH", "Firestore UserProfile 생성 완료")
                _currentUser.value = authResult.user
                if (onSuccess != null) {
                    onSuccess()
                }

            } catch (e: Exception) {
                Log.e("AUTH", "회원가입 실패", e)
                _errorMessage.value = "회원가입 실패: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 로그인
     */
    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                _currentUser.value = authResult.user

                Log.d("AUTH", "로그인 성공: ${authResult.user?.uid}")
                onSuccess()

            } catch (e: Exception) {
                Log.e("AUTH", "로그인 실패", e)
                _errorMessage.value = "로그인 실패: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 로그아웃
     */
    fun signOut(onSuccess: () -> Unit) {
        try {
            auth.signOut()
            _currentUser.value = null
            Log.d("AUTH", "로그아웃 성공")
            onSuccess()
        } catch (e: Exception) {
            Log.e("AUTH", "로그아웃 실패", e)
            _errorMessage.value = "로그아웃 실패: ${e.message}"
        }
    }

    /**
     * 현재 로그인된 사용자 가져오기
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    /**
     * 현재 사용자 UID 가져오기 (편의 함수)
     */
    fun getCurrentUid(): String? {
        return auth.currentUser?.uid
    }

    /**
     * 로그인 상태 확인
     */
    fun isSignedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * 에러 메시지 초기화
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
