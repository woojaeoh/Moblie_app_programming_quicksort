package com.example.quicksort

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 배경 레이어 (HomeScreen과 동일한 스타일)
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(4.5f)
                    .fillMaxWidth()
                    .background(Color(0xFFBCDFAC))
            )
            Box(
                modifier = Modifier
                    .weight(4.7f)
                    .fillMaxWidth()
                    .background(Color(0xFFE6E9D3))
            )
            Box(
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxWidth()
                    .background(Color(0xFF8F7669))
            )
        }

        // 배경 이미지 (이미지 리소스 추가 필요)
        // Image(
        //     painter = painterResource(id = R.drawable.tree3),
        //     contentDescription = null,
        //     modifier = Modifier
        //         .fillMaxWidth()
        //         .align(Alignment.BottomCenter)
        //         .offset(y = (-30).dp)
        // )

        // 메인 콘텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 앱 제목
            Text(
                text = "QuickSort",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D5016)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "분리수거를 쉽고 빠르게",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    color = Color(0xFF5A7C3E)
                )
            )

            Spacer(modifier = Modifier.height(80.dp))

            // 로그인 버튼
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(
                    text = "로그인",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 회원가입 버튼
            OutlinedButton(
                onClick = onSignUpClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF4CAF50)
                )
            ) {
                Text(
                    text = "회원가입",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }
        }
    }
}
