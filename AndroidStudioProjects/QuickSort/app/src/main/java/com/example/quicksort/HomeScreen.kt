package com.example.quicksort

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle



@Composable
fun HomeScreen(
    onCameraClick: () -> Unit,
    onStatsClick: () -> Unit,
    authViewModel: AuthViewModel,
    aiViewModel: AiViewModel,
    onHistoryClick: () -> Unit = {}
) {
    var userName by remember { mutableStateOf("사용자") }
    var CO2 by remember { mutableStateOf(0.0) }

    val currentUser by authViewModel.currentUser.collectAsState()

    // 사용자 정보 로드
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            aiViewModel.getUserInfo(user.uid) { userProfile ->
                userName = userProfile.username
                CO2 = userProfile.totalCarbonReduced
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 1층 → 화면 상단 큰 영역 (가중치 6)
            Box(
                modifier = Modifier
                    .weight(4.5f)
                    .fillMaxWidth()
                    .background(Color(0xFFBCDFAC))
            )

            // 2층 → 중간 레이어 (가중치 3)
            Box(
                modifier = Modifier
                    .weight(4.7f)
                    .fillMaxWidth()
                    .background(Color(0xFFE6E9D3))
            )

            // 3층 → 맨 아래 얇은 레이어 (가중치 1)
            Box(
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxWidth()
                    .background(Color(0xFF8F7669))
            )
        }


        // ④ 배경 이미지 - CO2에 따라 나무 성장!
        val treeResource = when {
            CO2 < 2.0 -> R.drawable.tree1
            CO2 < 4.0 -> R.drawable.tree2
            CO2 < 6.0 -> R.drawable.tree3
            CO2 < 8.0 -> R.drawable.tree4
            else -> R.drawable.tree5
        }

        Image(
            painter = painterResource(id = treeResource),
            contentDescription = "나무 - ${CO2}kg CO2 절감",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .offset(y = (-30).dp)
        )

        // ⑤ 실제 UI 콘텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp)
        ) {
            // 로고
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp)
                    .size(120.dp)
            )


            // 상단 텍스트
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 8.dp, end = 24.dp)
            ) {
                Text(
                    text = "안녕하세요, $userName 님!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = "지금까지 ${userName}님이 절감한 CO₂는",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 20.sp,
                    )
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append("$CO2")
                        }
                        append("kg 이에요.")
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 24.sp
                    )
                )

            }

            Spacer(modifier = Modifier.height(42.dp))

            // 메뉴 카드 2개
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                HomeMenuCard(
                    icon = Icons.Default.CameraAlt,
                    title = "촬영하기",
                    subtitle = "이미지 인식으로 분리수거\n방법을 알려 드려요.",
                    onClick = onCameraClick
                )

                HomeMenuCard(
                    icon = Icons.Default.ShowChart,
                    title = "내 통계",
                    subtitle = "내 분리수거 성향을\n알아보아요.",
                    onClick = onStatsClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 히스토리 카드 (가로로 길게)
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(100.dp)
                    .clickable { onHistoryClick() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "내 기록",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "저장한 분리수거 사진을 확인하세요.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}



@Composable
fun HomeMenuCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier
            .width(170.dp)
            .height(150.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}




