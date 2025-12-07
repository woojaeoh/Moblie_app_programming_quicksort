# QuickSort Backend API 명세서

## 목차
1. [프로젝트 구조](#프로젝트-구조)
2. [인증 API (AuthViewModel)](#인증-api-authviewmodel)
3. [메인 기능 API (AiViewModel)](#메인-기능-api-aiviewmodel)
4. [AI 서버 연동](#ai-서버-연동)
5. [데이터베이스 구조](#데이터베이스-구조)
6. [프론트엔드 사용 가이드](#프론트엔드-사용-가이드)

---

## 프로젝트 구조

```
app/src/main/java/com/example/quicksort/
├── AuthViewModel.kt          # 인증 관련 ViewModel
├── AiViewModel.kt            # 메인 기능 ViewModel
├── models/
│   ├── UserProfile.kt        # 사용자 프로필 모델
│   ├── TrashHistory.kt       # 분리수거 기록 모델
│   └── TrashGuide.kt         # 분리수거 가이드 모델
└── repository/
    ├── UserRepository.kt           # 사용자 CRUD
    ├── TrashHistoryRepository.kt   # 기록 CRUD
    ├── TrashGuideRepository.kt     # 가이드 조회
    └── StorageRepository.kt        # 이미지 업로드/삭제
```

---

## 인증 API (AuthViewModel)

### 1. 회원가입

**함수:** `signUp(email: String, password: String, username: String, onSuccess: () -> Unit)`

**설명:** Firebase Authentication 계정 생성 + Firestore UserProfile 자동 생성

**호출 템플릿:**
```kotlin
@Composable
fun SignUpScreen() {
    val authViewModel: AuthViewModel = viewModel()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }

    Column {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") }
        )

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            visualTransformation = PasswordVisualTransformation()
        )

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("사용자 이름") }
        )

        Button(
            onClick = {
                authViewModel.signUp(email, password, username) {
                    // 회원가입 성공 시 메인 화면으로 이동
                    navController.navigate("main")
                }
            },
            enabled = !isLoading
        ) {
            Text(if (isLoading) "처리 중..." else "회원가입")
        }

        // 에러 메시지 표시
        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
```

**동작 흐름:**
1. Firebase Authentication 계정 생성
2. Firestore `users/{uid}` 문서에 UserProfile 생성
3. 실패 시 Authentication 계정 자동 롤백

---

### 2. 로그인

**함수:** `signIn(email: String, password: String, onSuccess: () -> Unit)`

**설명:** 이메일/비밀번호로 로그인

**호출 템플릿:**
```kotlin
@Composable
fun SignInScreen() {
    val authViewModel: AuthViewModel = viewModel()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") }
        )

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            visualTransformation = PasswordVisualTransformation()
        )

        Button(
            onClick = {
                authViewModel.signIn(email, password) {
                    // 로그인 성공 시 메인 화면으로 이동
                    navController.navigate("main")
                }
            },
            enabled = !isLoading
        ) {
            Text(if (isLoading) "로그인 중..." else "로그인")
        }

        errorMessage?.let {
            Text(text = it, color = Color.Red)
        }
    }
}
```

---

### 3. 로그아웃

**함수:** `signOut(onSuccess: () -> Unit)`

**설명:** Firebase 로그아웃 + currentUser 초기화

**호출 템플릿:**
```kotlin
@Composable
fun ProfileScreen() {
    val authViewModel: AuthViewModel = viewModel()

    Button(onClick = {
        authViewModel.signOut {
            // 로그아웃 성공 시 로그인 화면으로 이동
            navController.navigate("login") {
                popUpTo("main") { inclusive = true }
            }
        }
    }) {
        Text("로그아웃")
    }
}
```

---

### 4. 로그인 상태 확인

**함수:** `isSignedIn(): Boolean`

**설명:** 현재 로그인 여부 확인

**호출 템플릿:**
```kotlin
@Composable
fun App() {
    val authViewModel: AuthViewModel = viewModel()

    // 앱 시작 시 로그인 상태에 따라 화면 분기
    if (authViewModel.isSignedIn()) {
        MainScreen()
    } else {
        SignInScreen()
    }
}
```

---

### 5. 현재 사용자 UID 가져오기

**함수:** `getCurrentUid(): String?`

**설명:** 현재 로그인된 사용자의 UID 반환

**호출 템플릿:**
```kotlin
val authViewModel: AuthViewModel = viewModel()
val uid = authViewModel.getCurrentUid() ?: return

// UID를 다른 API 호출에 사용
aiViewModel.getUserInfo(uid) { userProfile ->
    // 사용자 정보 처리
}
```

---

## 메인 기능 API (AiViewModel)

### 1. 이미지 분석 (1단계)

**함수:** `analyzeRecycling(uid: String, imageUri: Uri, context: Context, onResult: (RecyclingResult?) -> Unit)`

**설명:**
- Firebase Storage에 이미지 업로드
- AI 서버로 이미지 분석 요청
- 분리수거 가이드 조회
- 탄소 절감량 계산
- **주의: 저장은 하지 않음** (사용자가 저장 버튼을 눌러야 함)

**호출 템플릿:**
```kotlin
@Composable
fun CameraScreen() {
    val aiViewModel: AiViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current

    val isLoading by aiViewModel.isLoading.collectAsState()
    val errorMessage by aiViewModel.errorMessage.collectAsState()

    var analysisResult by remember { mutableStateOf<AiViewModel.RecyclingResult?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }

    // 이미지 선택 런처
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val uid = authViewModel.getCurrentUid() ?: return@rememberLauncherForActivityResult

            // 이미지 분석 시작
            aiViewModel.analyzeRecycling(uid, imageUri, context) { result ->
                if (result != null) {
                    analysisResult = result
                    showResultDialog = true
                } else {
                    // 에러 처리 (errorMessage StateFlow 확인)
                }
            }
        }
    }

    Column {
        Button(
            onClick = { launcher.launch(imageUri) },
            enabled = !isLoading
        ) {
            Text(if (isLoading) "분석 중..." else "사진 촬영")
        }

        errorMessage?.let {
            Text(text = it, color = Color.Red)
        }
    }

    // 분석 결과 다이얼로그
    if (showResultDialog && analysisResult != null) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = { Text("분석 결과") },
            text = {
                Column {
                    Text("카테고리: ${analysisResult!!.category}")
                    Text("상세: ${analysisResult!!.detail}")
                    Text("탄소 절감: ${analysisResult!!.carbonReduced}kg CO₂")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("가이드:")
                    analysisResult!!.guide.forEach { guide ->
                        Text("• $guide")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    // 저장 버튼 클릭 시
                    val uid = authViewModel.getCurrentUid() ?: return@Button
                    aiViewModel.saveToHistory(uid, analysisResult!!) {
                        showResultDialog = false
                        // 저장 완료 메시지 표시
                    }
                }) {
                    Text("저장")
                }
            },
            dismissButton = {
                Button(onClick = {
                    // 저장 안 함 버튼 클릭 시 이미지 삭제
                    aiViewModel.cancelAndDeleteImage(analysisResult!!.imageUrl)
                    showResultDialog = false
                }) {
                    Text("저장 안 함")
                }
            }
        )
    }
}
```

**RecyclingResult 데이터 구조:**
```kotlin
data class RecyclingResult(
    val imageUrl: String,        // Firebase Storage URL
    val category: String,        // 분리수거 카테고리
    val detail: String,          // 상세 품목
    val guide: List<String>,     // 분리수거 가이드
    val carbonReduced: Double    // 탄소 절감량 (kg CO₂)
)
```

---

### 2. 분석 결과 저장 (2단계)

**함수:** `saveToHistory(uid: String, result: RecyclingResult, onSuccess: () -> Unit)`

**설명:**
- 분석 결과를 Firestore trash_history에 저장
- totalCarbonReduced 자동 업데이트

**호출 템플릿:**
```kotlin
// analyzeRecycling() 결과를 받은 후 사용자가 저장 버튼을 누를 때
val uid = authViewModel.getCurrentUid() ?: return

aiViewModel.saveToHistory(uid, analysisResult) {
    // 저장 성공
    Toast.makeText(context, "저장되었습니다!", Toast.LENGTH_SHORT).show()
    navController.navigate("history")
}
```

---

### 3. 이미지 삭제 (저장 안 함)

**함수:** `cancelAndDeleteImage(imageUrl: String)`

**설명:** 사용자가 분석 결과를 저장하지 않을 때 업로드한 이미지 삭제

**호출 템플릿:**
```kotlin
// 사용자가 "저장 안 함" 버튼을 누를 때
aiViewModel.cancelAndDeleteImage(analysisResult.imageUrl)
```

---

### 4. 사용자 기록 조회 (갤러리)

**함수:** `getUserHistory(uid: String, onResult: (List<TrashHistory>) -> Unit)`

**설명:** 사용자의 분리수거 기록 조회 (최신순)

**호출 템플릿:**
```kotlin
@Composable
fun HistoryScreen() {
    val aiViewModel: AiViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    var historyList by remember { mutableStateOf<List<TrashHistory>>(emptyList()) }

    LaunchedEffect(Unit) {
        val uid = authViewModel.getCurrentUid() ?: return@LaunchedEffect

        aiViewModel.getUserHistory(uid) { histories ->
            historyList = histories
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // 인스타그램 스타일 3분할
        contentPadding = PaddingValues(4.dp)
    ) {
        items(historyList) { history ->
            AsyncImage(
                model = history.image_url,
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(2.dp)
                    .clickable {
                        // 상세 화면으로 이동
                        navController.navigate("detail/${history.id}")
                    }
            )
        }
    }
}
```

**TrashHistory 데이터 구조:**
```kotlin
data class TrashHistory(
    val id: String,
    val image_url: String,
    val category: String,
    val detail: String,
    val guide: List<String>,
    val carbonReduced: Double,
    val date: String  // yyyy-MM-dd 형식
)
```

---

### 5. 기록 삭제

**함수:** `deleteHistory(uid: String, historyId: String, imageUrl: String)`

**설명:**
- Storage에서 이미지 삭제
- Firestore에서 기록 삭제
- **totalCarbonReduced 자동 감소** (해당 기록의 carbonReduced 만큼)

**호출 템플릿:**
```kotlin
@Composable
fun HistoryDetailScreen(historyId: String) {
    val aiViewModel: AiViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    Button(onClick = {
        val uid = authViewModel.getCurrentUid() ?: return@Button

        aiViewModel.deleteHistory(uid, historyId, history.image_url)

        // 삭제 후 이전 화면으로 이동
        navController.popBackStack()
    }) {
        Text("삭제")
    }
}
```

---

### 6. 랭킹 조회

**함수:** `getRanking(limit: Int = 10, onResult: (List<UserProfile>) -> Unit)`

**설명:** CO₂ 절감량 순으로 정렬된 사용자 목록 조회

**호출 템플릿:**
```kotlin
@Composable
fun RankingScreen() {
    val aiViewModel: AiViewModel = viewModel()

    var rankingList by remember { mutableStateOf<List<UserProfile>>(emptyList()) }

    LaunchedEffect(Unit) {
        aiViewModel.getRanking(limit = 50) { users ->
            rankingList = users
        }
    }

    LazyColumn {
        itemsIndexed(rankingList) { index, user ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Text(
                        text = "${index + 1}",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(40.dp)
                    )
                    Text(text = user.username)
                }
                Text(
                    text = "${user.totalCarbonReduced} kg CO₂",
                    color = Color.Green
                )
            }
        }
    }
}
```

**UserProfile 데이터 구조:**
```kotlin
data class UserProfile(
    val uid: String,
    val username: String,
    val email: String,
    val totalCarbonReduced: Double
)
```

---

### 7. 내 순위 조회

**함수:** `getMyRank(uid: String, onResult: (Int) -> Unit)`

**설명:** 전체 사용자 중 내 순위 조회

**호출 템플릿:**
```kotlin
@Composable
fun ProfileScreen() {
    val aiViewModel: AiViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    var myRank by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        val uid = authViewModel.getCurrentUid() ?: return@LaunchedEffect

        aiViewModel.getMyRank(uid) { rank ->
            myRank = rank
        }
    }

    myRank?.let {
        Text(
            text = "내 순위: ${it}위",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
```

---

### 8. 사용자 정보 조회

**함수:** `getUserInfo(uid: String, onResult: (UserProfile) -> Unit)`

**설명:** 사용자 프로필 정보 조회

**호출 템플릿:**
```kotlin
@Composable
fun ProfileScreen() {
    val aiViewModel: AiViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    var userProfile by remember { mutableStateOf<UserProfile?>(null) }

    LaunchedEffect(Unit) {
        val uid = authViewModel.getCurrentUid() ?: return@LaunchedEffect

        aiViewModel.getUserInfo(uid) { profile ->
            userProfile = profile
        }
    }

    userProfile?.let { profile ->
        Column {
            Text("이름: ${profile.username}")
            Text("이메일: ${profile.email}")
            Text("총 절감량: ${profile.totalCarbonReduced} kg CO₂")
        }
    }
}
```

---

## AI 서버 연동

### AI API 엔드포인트

**Retrofit 설정 위치:** `RetrofitClient.kt`

### 1. Ping (서버 상태 확인)

**엔드포인트:** `GET /ping`

**응답:**
```json
{
  "message": "pong"
}
```

---

### 2. 이미지 분석

**엔드포인트:** `POST /predict`

**요청 (AiRequest):**
```json
{
  "image_url": "https://firebasestorage.googleapis.com/.../image.jpg"
}
```

**응답 (AiResponse):**
```json
{
  "category": "플라스틱",
  "detail": "PET병"
}
```

**category 값 (예시):**
- `플라스틱`
- `종이`
- `캔`
- `유리`
- `비닐`
- `일반쓰레기`

**detail 값:**
- 카테고리별 세부 품목 (예: PET병, 종이박스, 알루미늄캔 등)

---

### AI 팀 요청사항

1. **baseUrl 설정 필요**
   - `RetrofitClient.kt`에서 AI 서버 주소 설정
   - 예: `http://your-ai-server.com/api/`

2. **이미지 전달 방식**
   - Firebase Storage에 업로드된 **공개 URL**을 전달
   - AI 서버에서 해당 URL로 이미지 다운로드 후 분석

3. **에러 처리**
   - HTTP 4xx/5xx 응답 시 적절한 에러 메시지 반환
   - 네트워크 타임아웃 설정 (현재 10초)

---

## 데이터베이스 구조

### Firestore 컬렉션 구조

```
users (컬렉션)
├── {uid} (문서) - UserProfile
│   ├── uid: String
│   ├── username: String
│   ├── email: String
│   ├── totalCarbonReduced: Double
│   └── trash_history (서브컬렉션)
│       └── {historyId} (문서) - TrashHistory
│           ├── id: String
│           ├── image_url: String
│           ├── category: String
│           ├── detail: String
│           ├── guide: List<String>
│           ├── carbonReduced: Double
│           └── date: String (yyyy-MM-dd)
│
trash_guide (컬렉션)
└── {category} (문서) - 예: "플라스틱", "종이" 등
    └── details (서브컬렉션)
        └── {detail} (문서) - 예: "PET병", "종이박스" 등
            └── description: List<String> - 분리수거 가이드 문자열 배열
```

**trash_guide 구조 예시:**
```
trash_guide/
├── 플라스틱/
│   └── details/
│       ├── PET병/
│       │   └── description: ["물로 헹구세요", "라벨을 제거하세요", "뚜껑은 따로 분리하세요"]
│       └── 비닐/
│           └── description: ["이물질을 제거하세요", "투명 비닐만 재활용 가능합니다"]
└── 종이/
    └── details/
        └── 종이박스/
            └── description: ["테이프를 제거하세요", "접어서 배출하세요"]
```

### Firebase Storage 구조

```
images/
└── {uid}/
    └── {timestamp}_{random}.jpg
```

---

## 프론트엔드 사용 가이드

### 1. ViewModel 인스턴스 생성

```kotlin
@Composable
fun MyScreen() {
    val authViewModel: AuthViewModel = viewModel()
    val aiViewModel: AiViewModel = viewModel()

    // ...
}
```

---

### 2. StateFlow 관찰

모든 ViewModel은 다음 StateFlow를 제공합니다:

```kotlin
val isLoading: StateFlow<Boolean>     // 로딩 중 여부
val errorMessage: StateFlow<String?>  // 에러 메시지
```

**사용 예시:**
```kotlin
val isLoading by aiViewModel.isLoading.collectAsState()
val errorMessage by aiViewModel.errorMessage.collectAsState()

// 로딩 인디케이터 표시
if (isLoading) {
    CircularProgressIndicator()
}

// 에러 메시지 표시
errorMessage?.let {
    Text(text = it, color = Color.Red)
}
```

---

### 3. 에러 메시지 초기화

```kotlin
authViewModel.clearError()  // AuthViewModel 에러 초기화
```

---

### 4. 전체 플로우 예시

#### 회원가입 → 로그인 → 이미지 분석 → 저장 → 랭킹 확인

```kotlin
// 1. 회원가입
authViewModel.signUp(email, password, username) {
    navController.navigate("main")
}

// 2. 이미지 촬영 및 분석
aiViewModel.analyzeRecycling(uid, imageUri, context) { result ->
    if (result != null) {
        // 3. 분석 결과 저장
        aiViewModel.saveToHistory(uid, result) {
            Toast.makeText(context, "저장 완료!", Toast.LENGTH_SHORT).show()
        }
    }
}

// 4. 랭킹 조회
aiViewModel.getRanking(limit = 10) { users ->
    // 랭킹 리스트 표시
}

// 5. 내 순위 조회
aiViewModel.getMyRank(uid) { rank ->
    // 내 순위 표시
}
```

---

## 주요 주의사항

### 1. 이미지 분석 → 저장 2단계 플로우

- `analyzeRecycling()`: 분석만 수행 (저장 안 함)
- `saveToHistory()`: 사용자가 저장 버튼을 누를 때 호출
- `cancelAndDeleteImage()`: 저장 안 할 때 이미지 삭제

### 2. 탄소 절감량 자동 업데이트

- 저장 시: `totalCarbonReduced` 자동 증가
- 삭제 시: `totalCarbonReduced` 자동 감소

### 3. UID 필수

대부분의 API는 사용자 UID가 필요합니다:
```kotlin
val uid = authViewModel.getCurrentUid() ?: return
```

### 4. Context 전달 (이미지 업로드)

`analyzeRecycling()`은 Context가 필요합니다:
```kotlin
val context = LocalContext.current
aiViewModel.analyzeRecycling(uid, imageUri, context) { ... }
```

---

## 의존성 (build.gradle.kts)

```kotlin
// Firebase
implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
implementation("com.google.firebase:firebase-firestore-ktx:25.1.0")
implementation("com.google.firebase:firebase-storage")
implementation("com.google.firebase:firebase-analytics")

// 코루틴
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

// Retrofit (AI 서버 통신)
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

// ViewModel Compose
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

// 이미지 로딩 (Coil - 추천)
implementation("io.coil-kt:coil-compose:2.5.0")
```

---

## 연락처

- **백엔드 담당:** [이름]
- **프론트엔드 담당:** [이름]
- **AI 담당:** [이름]

---

**문서 최종 수정일:** 2025-12-03
