package com.example.quicksort.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.quicksort.R

// Urbanist 폰트 패밀리
val Urbanist = FontFamily(
    Font(R.font.urbanistregular, FontWeight.Normal),
    Font(R.font.urbanistmedium, FontWeight.Medium),
    Font(R.font.urbanistlight, FontWeight.Light),
    Font(R.font.urbanistsemibold, FontWeight.SemiBold),
)

// Material Typography 재정의
val Typography = Typography(

    // 예시: bodyLarge 기본 스타일
    bodyLarge = TextStyle(
        fontFamily = Urbanist,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    headlineMedium = TextStyle(
        fontFamily = Urbanist,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp
    ),

    headlineSmall = TextStyle(
        fontFamily = Urbanist,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = Urbanist,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp
    )
)
