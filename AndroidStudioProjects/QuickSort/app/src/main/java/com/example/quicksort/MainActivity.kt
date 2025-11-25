package com.example.quicksort

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.quicksort.ui.theme.QuickSortTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import androidx.lifecycle.viewmodel.compose.viewModel // ViewModel을 Composable에서 사용하기 위함

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuickSortTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TestScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun TestScreen(
    modifier:Modifier = Modifier,
    viewModel: AiViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    Column {
        Button(onClick = { viewModel.testPing() }) {
            Text("Ping 테스트")
        }
        Button(onClick = { viewModel.testPredict() }) {
            Text("Predict 테스트")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    QuickSortTheme {
        Greeting("Android")
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}