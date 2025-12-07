# AiViewModel.kt 수정

with open('app/src/main/java/com/example/quicksort/AiViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# mock 데이터 함수를 실제 AI 호출 함수로 교체
old_function = '''    /**
     * 간단한 analyzeRecycling 오버로드 (ResultScreen에서 사용)
     */
    fun analyzeRecycling(imageUri: Uri, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // AI 분석 (간단한 버전 - Storage 업로드 없이 로컬 분석)
                // 실제로는 AI 서버에 전송해야 하지만, 여기서는 analysisResult만 설정
                _analysisResult.value = AnalysisResult(
                    imageUri = imageUri,
                    category = "플라스틱",
                    detail = "페트병"
                )
                onComplete(true)
            } catch (e: Exception) {
                Log.e("AiViewModel", "분석 실패", e)
                onComplete(false)
            }
        }
    }'''

new_function = '''    /**
     * 간단한 analyzeRecycling 오버로드 (MainActivity에서 사용)
     * 실제 AI 분석 함수를 호출하고 결과를 StateFlow에 저장
     */
    fun analyzeRecyclingSimple(uid: String, imageUri: Uri, context: Context, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // 실제 AI 분석 함수 호출
                analyzeRecycling(uid, imageUri, context) { result ->
                    if (result != null) {
                        // AI에서 받은 실제 데이터로 analysisResult 설정
                        _analysisResult.value = AnalysisResult(
                            imageUri = imageUri,
                            category = result.category,  // AI 서버에서 받은 실제 데이터
                            detail = result.detail        // AI 서버에서 받은 실제 데이터
                        )
                        onComplete(true)
                    } else {
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                Log.e("AiViewModel", "분석 실패", e)
                onComplete(false)
            }
        }
    }'''

content = content.replace(old_function, new_function)

with open('app/src/main/java/com/example/quicksort/AiViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)

print("AiViewModel.kt 수정 완료!")
