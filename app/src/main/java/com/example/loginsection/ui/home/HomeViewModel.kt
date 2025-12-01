package com.example.loginsection.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginsection.data.model.TrashHistory
import com.example.loginsection.data.model.UserProfile
import com.example.loginsection.data.repository.TrashHistoryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val profile: UserProfile? = null,
    val trashHistoryList: List<TrashHistory> = emptyList(),
)

//TrashHistoryRepository 정보를 가져오는 모델
class HomeViewModel(
    private val repository: TrashHistoryRepository = defaultRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var historyListener: ListenerRegistration? = null

    private var profileLoaded = false
    private var historyLoaded = false

    //HomeScreen.kt가 실행될때 자동으로 한번 호출ㅅ
    init {
        loadUserProfile()
        observeTrashHistory()
    }

    private fun setProfileLoaded() {
        profileLoaded = true
        updateLoadingState()
    }

    private fun setHistoryLoaded() {
        historyLoaded = true
        updateLoadingState()
    }

    private fun updateLoadingState() {
        if (profileLoaded && historyLoaded) {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadUserProfile() {
        repository.loadUserProfile { result ->
            viewModelScope.launch {
                result
                    .onSuccess { profile ->
                        _uiState.update {
                            it.copy(
                                profile = profile,
                                errorMessage = null
                            )
                        }
                        setProfileLoaded()
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                errorMessage = e.message ?: "프로필을 불러오지 못했습니다."
                            )
                        }
                        setProfileLoaded()
                    }
            }
        }
    }

    private fun observeTrashHistory() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        historyListener = repository.listenMyTrashHistory { result ->
            viewModelScope.launch {
                result
                    .onSuccess { list ->

                        if (list.isEmpty()) {
                            repository.addDummyHistories {}
                            return@onSuccess
                        }

                        _uiState.update {
                            it.copy(
                                trashHistoryList = list,
                                errorMessage = null
                            )
                        }
                        setHistoryLoaded()
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                errorMessage = e.message ?: "기록을 불러오지 못했습니다."
                            )
                        }
                        setHistoryLoaded()
                    }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        historyListener?.remove()
    }

    companion object {
        private fun defaultRepository(): TrashHistoryRepository {
            val auth = FirebaseAuth.getInstance()
            val db = FirebaseFirestore.getInstance()
            return TrashHistoryRepository(auth, db)
        }
    }
}