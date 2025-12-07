package com.example.quicksort

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import coil.compose.rememberAsyncImagePainter



@Composable
fun ResultScreen(
    onConfirmClick: () -> Unit,
    aiViewModel: AiViewModel,
    authViewModel: AuthViewModel
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var itemName by remember { mutableStateOf("분석 중...") }
    var category by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var descriptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val analysisResult by aiViewModel.analysisResult.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    // 분석 결과 로드
    LaunchedEffect(analysisResult) {
        analysisResult?.let { result ->
            category = result.category
            detail = result.detail
            itemName = detail.ifEmpty { category }
            imageUri = result.imageUri

            // 가이드 가져오기
            aiViewModel.getGuideForCategory(category) { guideList ->
                descriptions = guideList
                isLoading = false
            }
        }
    }

    // 체크박스 상태 리스트
    val checkedStates = remember { mutableStateListOf<Boolean>() }

    LaunchedEffect(descriptions) {
        checkedStates.clear()
        checkedStates.addAll(List(descriptions.size) { false })
    }

    val allChecked = checkedStates.isNotEmpty() && checkedStates.all { it }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {

        // 상단 제목
        Text(
            text = "분석 결과",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 20.dp, bottom = 16.dp)
        )

        // 스크롤 가능한 영역
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {

            // 로딩 상태
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // 분석 이미지
                imageUri?.let { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(bottom = 24.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // 물건 이름
            Text(
                text = itemName,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )
            )

            Text(
                text = "이 물건이 아닌가요?",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 설명 + 체크박스 목록
            descriptions.forEachIndexed { index, desc ->
                if (index < checkedStates.size) {  // ← 이 줄 추가
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checkedStates[index],
                            onCheckedChange = {
                                checkedStates[index] = it
                            }
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
                }
        }

        // 하단 버튼
        Button(
            onClick = {
                if (allChecked) {
                    // 분리수거 기록 저장
                    currentUser?.let { user ->
                        analysisResult?.let { result ->
                            aiViewModel.saveToHistory(
                                uid = user.uid,
                                imageUrl = result.imageUri.toString(),
                                category = category,
                                detail = detail,
                                guide = descriptions.joinToString("\n"),
                                carbonReduced = aiViewModel.calculateCarbonReduction(category)
                            ) { success ->
                                if (success) {
                                    onConfirmClick()
                                }
                            }
                        }
                    }
                }
            },
            enabled = allChecked && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .height(56.dp)
        ) {
            Text(
                text = "분리수거하기!",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }

