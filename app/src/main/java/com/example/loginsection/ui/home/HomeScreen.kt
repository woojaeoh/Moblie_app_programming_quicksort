package com.example.loginsection.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.loginsection.data.model.TrashHistory

//홈 화면 UI만
//내부 로직은 HomeViewModel이 TrashHistoryRepository를 가져와 처리
@Composable
fun HomeScreen(
    onLogoutClick: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.errorMessage != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(uiState.errorMessage ?: "알 수 없는 오류")
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        else -> {
            val profile = uiState.profile

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                // --- 프로필 영역 ---
                if (profile != null) {
                    Text(
                        "환영합니다, ${profile.id} 님 👋",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("이메일: ${profile.email}")
                    Spacer(Modifier.height(8.dp))
                    Text("포인트: ${profile.points} 점")
                } else {
                    Text(
                        "프로필 정보를 불러올 수 없습니다.",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(Modifier.height(24.dp))

                // --- 분리배출 내역 영역 ---
                Text(
                    "분리배출 내역",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))

                if (uiState.trashHistoryList.isEmpty()) {
                    Text("아직 분리배출 내역이 없습니다.")
                } else {
                    uiState.trashHistoryList.forEach { history ->
                        TrashHistoryItem(history)
                        Spacer(Modifier.height(12.dp))
                    }
                }

                Spacer(Modifier.height(24.dp))

                Spacer(Modifier.height(24.dp))

                Button(onClick = onLogoutClick) {
                    Text("로그아웃")
                }
            }
        }
    }
}

@Composable
fun TrashHistoryItem(history: TrashHistory) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "[${history.category}] ${history.detail}",
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "가이드: ${history.guide}",
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "포인트: ${history.points_earned} · 날짜: ${history.date}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}