package com.example.quicksort

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.quicksort.models.TrashHistory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHistoryScreen(
    authViewModel: AuthViewModel,
    aiViewModel: AiViewModel,
    onBackClick: () -> Unit
) {
    val uid = authViewModel.getCurrentUid()
    var histories by remember { mutableStateOf<List<TrashHistory>>(emptyList()) }
    var selectedHistory by remember { mutableStateOf<TrashHistory?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 로그인 안 한 경우
    if (uid == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE6E9D3))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
        aiViewModel.getUserHistory(uid) { result ->
            histories = result
        }
    }

    // 삭제 확인 다이얼로그
    if (showDeleteDialog && selectedHistory != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("기록 삭제") },
            text = { Text("이 분리수거 기록을 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        aiViewModel.deleteHistory(uid, selectedHistory!!.id, selectedHistory!!.image_url)
                        histories = histories.filter { it.id != selectedHistory!!.id }
                        showDeleteDialog = false
                        selectedHistory = null
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // 상세 보기 다이얼로그
    selectedHistory?.let { history ->
        if (!showDeleteDialog) {
            Dialog(onDismissRequest = { selectedHistory = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        // 이미지
                        AsyncImage(
                            model = history.image_url,
                            contentDescription = "분리수거 이미지",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 카테고리
                        Text(
                            text = "카테고리: ${history.category}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 상세정보
                        Text(
                            text = "상세: ${history.detail}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 날짜
                        Text(
                            text = "날짜: ${history.date}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // CO2 절감량
                        Text(
                            text = "CO₂ 절감: ${history.carbonReduced}kg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 가이드
                        if (history.guide.isNotEmpty()) {
                            Text(
                                text = "분리수거 가이드:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            history.guide.forEach { guideItem ->
                                Text(
                                    text = "• $guideItem",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 버튼들
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = { selectedHistory = null }
                            ) {
                                Text("닫기")
                            }

                            TextButton(
                                onClick = { showDeleteDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "삭제",
                                    tint = Color.Red
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("삭제", color = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    // 메인 화면
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6E9D3))
            .padding(16.dp)
    ) {
        // 상단 바
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "내 분리수거 기록",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 그리드 갤러리 (3분할)
        if (histories.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "로고",
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "아직 저장된 분리수거 기록이 없습니다.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.DarkGray
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(histories) { history ->
                    AsyncImage(
                        model = history.image_url,
                        contentDescription = "${history.category} - ${history.detail}",
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedHistory = history },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
