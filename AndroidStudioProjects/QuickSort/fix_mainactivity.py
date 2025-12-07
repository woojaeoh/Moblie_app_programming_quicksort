# MainActivity.kt 수정

with open('app/src/main/java/com/example/quicksort/MainActivity.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# analyzeRecycling 호출 부분 수정
old_code = '''                onImageCaptured = { uri ->
                    // 이미지 촬영 후 AI 분석
                    aiViewModel.analyzeRecycling(uri) { success ->
                        if (success) {
                            navController.navigate("result") {
                                popUpTo("camera") { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "분석 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                },'''

new_code = '''                onImageCaptured = { uri ->
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
                },'''

content = content.replace(old_code, new_code)

with open('app/src/main/java/com/example/quicksort/MainActivity.kt', 'w', encoding='utf-8') as f:
    f.write(content)

print("MainActivity.kt 수정 완료!")
