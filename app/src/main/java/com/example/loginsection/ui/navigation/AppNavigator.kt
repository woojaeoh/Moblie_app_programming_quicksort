package com.example.loginsection.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.loginsection.ui.home.HomeScreen
import com.example.loginsection.ui.login.LoginScreen
import com.google.firebase.auth.FirebaseAuth
import com.example.loginsection.ui.splash.SplashScreen

//컨트롤러 역할
@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    val auth = remember { FirebaseAuth.getInstance() }

    NavHost(
        navController = navController,
        startDestination = "splash" // 맨 처음에 splash에서 시작
    ) {
        composable("splash") {
            // splash/splashScreen.kt 에서 auth.currentUser 없으면 로그인 화면, 있으면 홈 화면으로 보냄
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // 로그인 화면에서 로그인 성공 시 홈 화면으로 보냄
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        //홈 화면에서 로그아웃 시 로그인 화면으로 보냄
        composable("home") {
            HomeScreen(
                onLogoutClick = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}