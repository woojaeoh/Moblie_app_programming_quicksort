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
import com.example.quicksort.ui.theme.QuickSortTheme
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel


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
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val aiViewModel: AiViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        // 첫 화면 - Welcome
        composable("welcome") {
            WelcomeScreen(
                onLoginClick = { navController.navigate("login") },
                onSignUpClick = { navController.navigate("signup") }
            )
        }

        // 로그인 화면
        composable("login") {
            LoginScreen(
                onBackClick = { navController.popBackStack() },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        // 회원가입 화면
        composable("signup") {
            SignUpScreen(
                onBackClick = { navController.popBackStack() },
                onSignUpSuccess = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        // 홈 화면
        composable("home") {
            HomeScreen(
                onCameraClick = { navController.navigate("camera") },
                onStatsClick = { navController.navigate("stats") },
                onHistoryClick = { navController.navigate("history") },
                onLogoutClick = {
                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                authViewModel = authViewModel,
                aiViewModel = aiViewModel
            )
        }

        // 카메라 화면
        composable("camera") {
            CameraScreen(
                onBackClick = { navController.popBackStack() },
                onImageCaptured = { uri ->
                    // 이미지 촬영 후 AI 분석
                    val uid = authViewModel.getCurrentUid() ?: return@CameraScreen
                    aiViewModel.analyzeRecyclingSimple(uid, uri, context) { success ->
                        if (success) {
                            navController.navigate("result") {
                                popUpTo("camera") { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "분석 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                aiViewModel = aiViewModel
            )
        }

        // 결과 화면
        composable("result") {
            ResultScreen(
                onConfirmClick = {
                    Toast.makeText(context, "분리수거 완료!", Toast.LENGTH_SHORT).show()
                    navController.navigate("home") {
                        popUpTo("result") { inclusive = true }
                    }
                },
                aiViewModel = aiViewModel,
                authViewModel = authViewModel
            )
        }

        // 통계 화면
        composable("stats") {
            StatsScreen(
                authViewModel = authViewModel,
                aiViewModel = aiViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 히스토리 화면
        composable("history") {
            UserHistoryScreen(
                authViewModel = authViewModel,
                aiViewModel = aiViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

    }
}

