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
import androidx.compose.material.icons.filled.Logout
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
    onHistoryClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var userName by remember { mutableStateOf("사용자") }
    var CO2 by remember { mutableStateOf(0.0) }

    val currentUser by authViewModel.currentUser.collectAsState()

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

            // 1층
            Box(
                modifier = Modifier
                    .weight(4.5f)
                    .fillMaxWidth()
                    .background(Color(0xFFBCDFAC))
            )

            // 2층
            Box(
                modifier = Modifier
                    .weight(4.7f)
                    .fillMaxWidth()
                    .background(Color(0xFFE6E9D3))
            )

            // 3층
            Box(
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxWidth()
                    .background(Color(0xFF8F7669))
            )
        }

        // 나무 이미지
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


        // ------ 상단 UI ------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp)
        ) {

            // 로고 + 로그아웃
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(120.dp)
                )

                IconButton(
                    onClick = {
                        authViewModel.signOut {
                            onLogoutClick()
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "로그아웃",
                        tint = Color(0xFF5D4037),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // 텍스트 정보
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
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp)
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append("$CO2")
                        }
                        append("kg 이에요.")
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 24.sp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ---- 내 기록 / 내 통계 ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                HomeMenuCard(
                    icon = Icons.Default.History,
                    title = "내 기록",
                    subtitle = "저장한 분리수거 사진을\n확인하세요.",
                    onClick = onHistoryClick
                )

                HomeMenuCard(
                    icon = Icons.Default.ShowChart,
                    title = "내 통계",
                    subtitle = "내 분리수거 성향을\n알아보아요.",
                    onClick = onStatsClick
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        // FloatingActionButton
        FloatingActionButton(
            onClick = onCameraClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color(0xFFBCDFAC),
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "촬영하기",
                modifier = Modifier.size(28.dp)
            )
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

