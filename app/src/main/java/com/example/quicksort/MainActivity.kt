package com.example.quicksort

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quicksort.HomeScreen
import com.example.quicksort.ResultScreen
import com.example.quicksort.ui.theme.QuickSortTheme
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuickSortTheme {
                Surface(color = MaterialTheme.colorScheme.background) {

                    val navController = rememberNavController()

                    AppNavHost(navController)
                }
            }
        }
    }
}

@Composable
fun AppNavHost(navController: androidx.navigation.NavHostController) {

    val context = LocalContext.current   // ★ 여기서 context 가져오기

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onCameraClick = { navController.navigate("result") },
                onStatsClick = {},
                userName = "사용자",
                CO2 = 36.5
            )
        }

        composable("result") {
            ResultScreen(
                itemName = "비닐봉투",
                imageRes = R.drawable.trash_plastic_bag,
                descriptions = listOf(
                    "비닐봉투는 이물질을 제거하신 후 깨끗하고 건조한 상태로 배출해 주세요.",
                    "음식물이나 기름이 묻은 비닐은 재활용이 어려우니 일반쓰레기로 처리해 주세요.",
                    "라면봉지·과자봉지 등 단일 비닐 소재 포장재는 비닐류로 배출 가능하지만, 알루미늄 코팅 등 복합재질 제품은 일반쓰레기로 분류해 주세요."
                ),
                onConfirmClick = {
                    Toast.makeText(context, "분리수거 완료!", Toast.LENGTH_SHORT).show()

                    navController.navigate("home") {
                        popUpTo("result") { inclusive = true }
                    }
                }
            )
        }
    }
}

