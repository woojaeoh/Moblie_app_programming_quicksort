package com.example.quicksort

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.quicksort.AiViewModel.CategoryStat

@Composable
fun StatsScreen(
    authViewModel: AuthViewModel,
    aiViewModel: AiViewModel,
    onBackClick: () -> Unit
) {
    val uid = authViewModel.getCurrentUid()

    var stats by remember { mutableStateOf<List<CategoryStat>>(emptyList()) }

    // 로그인 안 한 경우
    if (uid == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE6E9D3))
                .padding(16.dp),
        ) {
            Text(
                text = "로그인이 필요합니다.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Red
            )
        }
        return
    }

    // 데이터 로드
    LaunchedEffect(uid) {
        aiViewModel.getCategoryStats(uid) { result ->
            stats = result
        }
    }

    val maxRatio = stats.maxOfOrNull { it.ratio } ?: 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6E9D3))
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ⭐ 왼쪽 위 로고
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(120.dp)   // 원하는 크기로 조정
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "분리수거 통계",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ⭐ 스크롤 가능한 공간
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (stats.isEmpty()) {
                item {
                    Text(
                        text = "아직 저장된 분리수거 기록이 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                }
            } else {
                items(stats) { stat ->
                    CategoryStatItem(
                        stat = stat,
                        isMax = stat.ratio == maxRatio
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryStatItem(
    stat: CategoryStat,
    isMax: Boolean
) {
    val barColor = if (isMax) Color(0xFF8F7669) else Color(0xFFBCDFAC)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 카테고리 이름
        Text(
            text = stat.category,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ⭐ 바 그래프
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .background(barColor.copy(alpha = 0.4f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(stat.ratio)
                    .background(barColor)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 오른쪽 정렬 비율 & 개수
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "비율: ${(stat.ratio * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${stat.count}개",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
    }
}
