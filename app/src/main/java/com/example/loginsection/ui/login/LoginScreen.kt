package com.example.loginsection.ui.login

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.loginsection.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

//로그인 화면
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    fun ensureProfileAndProceed(user: FirebaseUser?) {
        if (user == null) {
            Toast.makeText(context, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = user.uid
        val userEmail = user.email ?: email

        val docRef = db.collection("users").document(uid)
        docRef.get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    val profile = UserProfile(
                        uid = uid,
                        id = userEmail.substringBefore("@"),
                        email = userEmail,
                        points = 0L
                    )
                    docRef.set(profile)
                        .addOnSuccessListener {
                            Toast.makeText(context, "프로필 생성 완료!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "프로필 생성 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    onLoginSuccess()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "프로필 조회 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    //회원 가입
    fun signUp() {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                Toast.makeText(context, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                ensureProfileAndProceed(result.user)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "회원가입 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    //로그인 확인
    fun signIn() {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Toast.makeText(context, "로그인 성공!", Toast.LENGTH_SHORT).show()
                ensureProfileAndProceed(auth.currentUser)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "로그인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Firebase Login", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { signIn() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("로그인")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = { signUp() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("회원가입")
        }
    }
}