package com.example.quicksort

// AiViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Log

class AiViewModel : ViewModel() {

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
